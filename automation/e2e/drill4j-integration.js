import path from 'path';
import dotenv from 'dotenv';
import { fileURLToPath } from 'url';
import { randomUUID, createHash } from 'crypto';
import { test as baseTest, request as baseRequest } from '@playwright/test';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

dotenv.config({ path: path.resolve(__dirname, '.env') });
dotenv.config({ path: path.resolve(__dirname, '../../docker/ecommerce/.env') });

const configuredDrillApiUrl =
  process.env.DRILL_API_URL?.trim() || 'http://localhost:8090/api';
const DRILL_API_URL = configuredDrillApiUrl.replace(
  'http://host.docker.internal:8090/api',
  'http://localhost:8090/api'
);
const DRILL_API_KEY = process.env.DRILL_API_KEY?.trim();
const DRILL_GROUP_ID = process.env.DRILL_GROUP_ID?.trim() || 'ecommerce';
const DRILL_DASHBOARD_BASE_URL =
  process.env.DRILL_DASHBOARD_BASE_URL?.trim() || 'http://localhost:8091';

const SERVICES = ['ecommerce:backend:0.2.0', 'ecommerce:auth:0.2.0'];

const originalNewContext = baseRequest.newContext.bind(baseRequest);

if (!process.env.DRILL_SESSION_ID?.trim()) {
  process.env.DRILL_SESSION_ID = randomUUID();
  if (DRILL_API_KEY) {
    sendSessionMetadata(
      DRILL_API_URL,
      DRILL_API_KEY,
      DRILL_GROUP_ID,
      process.env.DRILL_SESSION_ID,
      process.env.DRILL_TEST_TASK_ID || 'playwright-tests'
    ).catch((error) => {
      console.error('Drill4J: error sending session metadata:', error);
    });
  } else {
    console.warn('Drill4J: DRILL_API_KEY is missing, metadata export is disabled.');
  }
}

const DRILL_SESSION_ID = process.env.DRILL_SESSION_ID.trim();
const DESIRED_PATH_SEPARATOR = '/';
const DRILL_ALLOWED_TEST_RESULTS = new Set([
  'PASSED',
  'FAILED',
  'ERROR',
  'SKIPPED',
  'SMART_SKIPPED',
  'UNKNOWN',
]);

function mapStatusToDrillResult(status) {
  const normalized = String(status ?? '').toUpperCase();
  return DRILL_ALLOWED_TEST_RESULTS.has(normalized) ? normalized : 'ERROR';
}

export const test = baseTest.extend({
  forEachWorker: [
    async ({}, use) => {
      global.drill = global.drill || {};
      global.drill.sessionId = DRILL_SESSION_ID;
      await use();
    },
    { scope: 'worker', auto: true },
  ],

  forEachTest: [
    async ({ page }, use, testInfo) => {
      testInfo.drillTestDefinitionId = generateDefinitionId(testInfo);
      testInfo.sessionId = global.drill.sessionId;
      testInfo.drillTestLaunchId = randomUUID();
      global.drill.testLaunchId = testInfo.drillTestLaunchId;

      const client = await page.context().newCDPSession(page);
      await client.send('Network.enable');
      await client.send('Network.setExtraHTTPHeaders', {
        headers: {
          'drill-test-id': testInfo.drillTestLaunchId,
          'drill-session-id': testInfo.sessionId,
        },
      });

      try {
        await use();
      } finally {
        if (DRILL_API_KEY) {
          await sendTestMetadata(
            DRILL_API_URL,
            DRILL_API_KEY,
            DRILL_GROUP_ID,
            global.drill.sessionId,
            testInfo
          );
        }
      }
    },
    { auto: true },
  ],
});

export const expect = test.expect;

baseRequest.newContext = async (options = {}) => {
  const extraHTTPHeaders = {
    ...options.extraHTTPHeaders,
  };

  if (global.drill?.sessionId) {
    extraHTTPHeaders['drill-session-id'] = global.drill.sessionId;
  }
  if (global.drill?.testLaunchId) {
    extraHTTPHeaders['drill-test-id'] = global.drill.testLaunchId;
  }

  return originalNewContext({
    ...options,
    extraHTTPHeaders,
  });
};

export { baseRequest as request };

async function sendSessionMetadata(apiAddress, apiKey, groupId, sessionId, testTaskId) {
  const payload = {
    id: sessionId,
    groupId,
    testTaskId,
    startedAt: new Date().toISOString(),
  };

  await sendHttpRequest('put', `${apiAddress}/data-ingest/sessions`, payload, {
    'X-Api-Key': apiKey,
  });
}

async function sendTestMetadata(apiAddress, apiKey, groupId, sessionId, testInfo) {
  const metadata = {
    titlePath: testInfo.titlePath.map((x) => normalizePath(x)),
    file: path.relative(__dirname, testInfo.file),
    name: normalizePath(testInfo.title),
  };

  const payload = {
    groupId,
    sessionId,
    tests: [
      {
        testLaunchId: testInfo.drillTestLaunchId,
        testDefinitionId: testInfo.drillTestDefinitionId,
        result: mapStatusToDrillResult(testInfo.status),
        duration: testInfo.duration,
        details: {
          runner: 'Playwright',
          path: metadata.file,
          testName: testInfo.title,
          tags: getTags(testInfo),
          metadata,
        },
      },
    ],
  };

  await sendHttpRequest(
    'post',
    `${apiAddress}/data-ingest/tests-metadata`,
    payload,
    { 'X-Api-Key': apiKey }
  );
}

function generateDefinitionId(testInfo) {
  const relativeFile = path.relative(__dirname, testInfo.file);
  const input = [...testInfo.titlePath, relativeFile]
    .map((x) => normalizePath(x))
    .join(DESIRED_PATH_SEPARATOR);

  return createHash('md5').update(input).digest('hex');
}

function getTags(testInfo) {
  return testInfo.tags && testInfo.tags.length > 0 ? [...testInfo.tags] : [];
}

function normalizePath(filePath) {
  return filePath.split(path.sep).join(DESIRED_PATH_SEPARATOR);
}

async function sendHttpRequest(method = 'post', url, body, additionalHeaders = {}) {
  const apiRequest = await originalNewContext({
    extraHTTPHeaders: additionalHeaders,
  });

  try {
    const response = await apiRequest[method](url, { data: body });
    if (!response.ok()) {
      throw new Error(`Request failed with status ${response.status()}`);
    }
  } catch (error) {
    console.error('Drill4J request error:', error.message);
  } finally {
    await apiRequest.dispose();
  }
}

function hasDrillQueryValue(value) {
  return value !== undefined && value !== null && String(value).trim() !== '';
}

const DRILL_DASHBOARD_QUERY_KEYS = [
  ['build', 'build'],
  ['path', 'path'],
  ['testResults', 'test_results'],
  ['testSession', 'test_session'],
  ['testDefinition', 'test_definition'],
  ['class', 'class'],
  ['package', 'package'],
];

export function formatDrillDashboardUrl(baseUrl, dashboardId, params = {}) {
  const origin = String(baseUrl).replace(/\/+$/, '');
  const id = String(dashboardId);
  const sp = new URLSearchParams();

  for (const [paramsKey, queryKey] of DRILL_DASHBOARD_QUERY_KEYS) {
    const value = params[paramsKey];
    if (hasDrillQueryValue(value)) {
      sp.set(queryKey, String(value).trim());
    }
  }

  const qs = sp.toString();
  return qs ? `${origin}/dashboard/${id}?${qs}` : `${origin}/dashboard/${id}`;
}

export function formatDrillSessionDashboardDescription() {
  return SERVICES.map((build) =>
    `[${build} coverage](${formatDrillDashboardUrl(DRILL_DASHBOARD_BASE_URL, '9', {
      build,
      testSession: process.env.DRILL_SESSION_ID,
    })})`
  ).join('\n');
}

export default {};

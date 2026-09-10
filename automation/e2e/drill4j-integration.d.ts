declare module '#drill4j-integration' {
  export const test: typeof import('@playwright/test').test;
  export const expect: typeof import('@playwright/test').expect;
  export const request: typeof import('@playwright/test').request;

  export interface DrillDashboardParams {
    build?: string;
    path?: string;
    testResults?: string;
    testSession?: string;
    testDefinition?: string;
    class?: string;
    package?: string;
  }

  export function formatDrillDashboardUrl(
    baseUrl: string,
    dashboardId: string,
    params?: DrillDashboardParams
  ): string;

  export function formatDrillSessionDashboardDescription(): string;

  const _default: Record<string, never>;
  export default _default;
}

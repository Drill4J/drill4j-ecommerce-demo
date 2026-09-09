import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: '#f8fafc',
    padding: 16,
  },
  card: {
    backgroundColor: '#ffffff',
    borderColor: '#e2e8f0',
    borderWidth: 1,
    borderRadius: 10,
    padding: 12,
    marginBottom: 12,
  },
  heading: {
    fontSize: 24,
    fontWeight: '700',
    marginBottom: 8,
    color: '#0f172a',
  },
  subHeading: {
    fontSize: 18,
    fontWeight: '600',
    marginBottom: 8,
    color: '#0f172a',
  },
  muted: {
    color: '#64748b',
    marginBottom: 4,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 8,
  },
  button: {
    backgroundColor: '#2563eb',
    borderRadius: 8,
    paddingVertical: 10,
    paddingHorizontal: 14,
    marginTop: 8,
  },
  buttonSecondary: {
    backgroundColor: '#e2e8f0',
  },
  buttonText: {
    color: '#ffffff',
    fontWeight: '600',
    textAlign: 'center',
  },
  buttonTextSecondary: {
    color: '#0f172a',
  },
  input: {
    borderColor: '#cbd5e1',
    borderWidth: 1,
    borderRadius: 8,
    paddingHorizontal: 10,
    paddingVertical: 8,
    backgroundColor: '#ffffff',
    marginBottom: 8,
  },
  error: {
    color: '#b91c1c',
    marginBottom: 8,
  },
  price: {
    fontWeight: '700',
    color: '#0f172a',
  },
});

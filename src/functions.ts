const aggregatedFieldsPrefixes = {
  sum: 'sum___',
  count: 'count___',
  avg: 'avg___',
  max: 'max___',
  min: 'min___',
};

export function Sum(fieldName: string): string {
  return `${aggregatedFieldsPrefixes.sum}${fieldName}`;
}

export function Count(fieldName: string): string {
  return `${aggregatedFieldsPrefixes.count}${fieldName}`;
}

export function Average(fieldName: string): string {
  return `${aggregatedFieldsPrefixes.avg}${fieldName}`;
}

export function Maximum(fieldName: string): string {
  return `${aggregatedFieldsPrefixes.max}${fieldName}`;
}

export function Minimum(fieldName: string): string {
  return `${aggregatedFieldsPrefixes.min}${fieldName}`;
}
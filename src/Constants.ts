export enum Relationship {
  and = 0,
  or = 1,
}
export enum ComparisonOperators {
  equalsTo = 0,
  equalsToOrGreaterThan = 1,
  equalsToOrSmallerThan = 2,
  greaterThan = 3,
  smallerThan = 4,
  startsWith = 5,
  endsWith = 6,
  contains = 7,
  in = 8,
  between = 9,
}

export enum SyncingDirection {
  pull = 0,
  push = 1,
  pullPush = 2,
}

export enum OrderByOptions {
  asc = 0,
  desc = 1,
}

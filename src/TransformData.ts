export function numberType(x: any): string {
  // check if the passed value is a number
  if (typeof x === 'number' && !isNaN(x)) {
    // check if it is integer
    if (Number.isSafeInteger(x)) {
      return 'int';
    } else {
      // could be double or float
      if (x % 1 === 0) {
        return 'double';
      }
      return 'float';
    }
  }
  return '';
}

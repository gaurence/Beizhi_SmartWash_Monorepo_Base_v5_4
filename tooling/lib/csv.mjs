export function parseCsv(text) {
  const rows = [];
  let current = [];
  let field = '';
  let inQuotes = false;

  const pushField = () => {
    current.push(field);
    field = '';
  };

  const pushRow = () => {
    // Skip trailing empty line
    if (current.length === 1 && current[0] === '' && rows.length > 0) {
      current = [];
      return;
    }
    rows.push(current);
    current = [];
  };

  const source = text.replace(/^\uFEFF/, '');

  for (let i = 0; i < source.length; i += 1) {
    const ch = source[i];

    if (inQuotes) {
      if (ch === '"') {
        if (source[i + 1] === '"') {
          field += '"';
          i += 1;
        } else {
          inQuotes = false;
        }
      } else {
        field += ch;
      }
      continue;
    }

    if (ch === '"') {
      inQuotes = true;
      continue;
    }

    if (ch === ',') {
      pushField();
      continue;
    }

    if (ch === '\n') {
      pushField();
      pushRow();
      continue;
    }

    if (ch !== '\r') {
      field += ch;
    }
  }

  if (field.length > 0 || current.length > 0) {
    pushField();
    pushRow();
  }

  return rows;
}

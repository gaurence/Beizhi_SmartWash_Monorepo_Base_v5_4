export function routePathForPage(pageKey) {
  return `/pages/${pageKey}`;
}

export function listChildren(pages, parentPageKey) {
  return pages.filter((page) => page.parentPage === parentPageKey);
}

export function findTopLevelPages(pages) {
  return pages.filter((page) => !page.parentPage);
}

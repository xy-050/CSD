export const CATEGORY_ICONS = {
    sugar: '🍬',
    bread: '🍞',
    milk: '🥛',
    egg: '🥚',
    rice: '🍚',
};

export function getCategoryIcon(category) {
    if (!category) return null;
    return CATEGORY_ICONS[String(category).toLowerCase()];
}

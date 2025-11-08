import { getCategoryIcon } from '../../../config/categoryIcons';

export function SearchResultsHeader({ keyword, originalCategory }) {
    const categoryIcon = getCategoryIcon(originalCategory || keyword);

    return (
        <div className="search-results-header" data-tour="search-results-header">
            {categoryIcon && (
                <span className="category-icon-large" aria-hidden="true">
                    {categoryIcon}
                </span>
            )}
            <h2>
                Search Results{keyword ? ` — ${keyword}` : ''}
            </h2>
        </div>
    );
}

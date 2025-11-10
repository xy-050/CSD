package app.favourites;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FavouritesRepository extends JpaRepository<Favourites, String> {
    @Query("""
            SELECT new app.favourites.FavouritesDTO(
                f.htsCode,
                COALESCE(p.description, 'No description available'),
                COALESCE(p.category, 'Unknown'),
                COALESCE(p.general, ''),
                COALESCE(p.special, '')
            )
            FROM Favourites f
            JOIN f.accounts a
            LEFT JOIN Product p ON p.htsCode = f.htsCode
            WHERE a.id = :accountId
            AND (p.fetchDate IS NULL OR p.fetchDate = (
                SELECT MAX(p2.fetchDate)
                FROM Product p2
                WHERE p2.htsCode = f.htsCode
            ))
            """)
    Optional<List<FavouritesDTO>> findFavouritesByAccountId(Integer accountId);

}

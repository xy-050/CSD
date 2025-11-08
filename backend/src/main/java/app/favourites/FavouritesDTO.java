package app.favourites;

public record FavouritesDTO(
    String htsCode,
    String description,
    String category,
    String general,
    String special
) {}

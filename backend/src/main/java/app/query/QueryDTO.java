package app.query;

public record QueryDTO(
    String htsCode,
    String description,
    String category,
    Long queryCount
) {}

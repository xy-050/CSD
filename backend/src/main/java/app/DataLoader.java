// package app;

// import java.time.LocalDate;
// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.ApplicationArguments;
// import org.springframework.boot.ApplicationRunner;
// import org.springframework.stereotype.Component;

// import app.product.*;
// import app.fta.*;

// /**
//  * This class runs fetchDaily() when the app is run.
//  * TODO: REMOVE WHEN MOVING TO MYSQL
//  */
// @Component
// public class DataLoader implements ApplicationRunner {

//     @Autowired
//     private ProductService productService;

//     @Autowired
//     private ProductRepository productRepository;

//     @Autowired
//     private FTARepository ftaRepository;

//     @Override
//     public void run(ApplicationArguments args) throws Exception {
//         // load products
//         productService.fetchExternal();
//         List<Product> products = productRepository.findAll();

//         for (Product product : products) {
//             product.setFetchDate(LocalDate.of(2022, 1, 1));
//             productRepository.save(product);

//             product.setFetchDate(LocalDate.of(2023, 1, 1));
//             if (product.getHtsCode().equals("0407.11.00.00")) {
//                 product.setGeneral("2.6¢/doz.");
//             }
//             productRepository.save(product);
//         }

//         // load ftas
//         List<FTA> ftas = List.of(
//             new FTA("AU", "0407.11.00.00", "Free", LocalDate.of(2028, 1, 1)),
//             new FTA("NZ", "0407.11.00.00", "2¢/doz", LocalDate.of(2026, 1, 1)),
//             new FTA("NZ", "0407.11.00.00", "1.4¢/doz", LocalDate.of(2028, 1, 1)),
//             new FTA("NZ", "0407.11.00.00", "1.2¢/doz", LocalDate.of(2030, 1, 1)),
//             new FTA("NZ", "0407.11.00.00", "1.1¢/doz", LocalDate.of(2031, 1, 1)),
//             new FTA("NZ", "0407.11.00.00", "0.7¢/doz", LocalDate.of(2033, 1, 1)),
//             new FTA("NZ", "0407.11.00.00", "Free", LocalDate.of(2035, 1, 1))
//         );

//         for (FTA fta : ftas) {
//             ftaRepository.save(fta);
//         }
//     }
// }
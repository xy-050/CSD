package app.favourites;

import org.springframework.stereotype.Service;

import app.account.Account;
import app.account.AccountRepository;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FavouritesService {
    private final AccountRepository accountRepository;
    private final FavouritesRepository favouritesRepository;

    /**
     * Constructor-based injection.
     * 
     * @param accountRepository Repository dependency.
     */
    public FavouritesService(AccountRepository accountRepository, FavouritesRepository favouritesRepository) {
        this.accountRepository = accountRepository;
        this.favouritesRepository = favouritesRepository;
    }

    /**
     * Saves the favourite hts code of user into their account
     * 
     * @param userId  Target user ID
     * @param htsCode Target HTS code
     */
    public void addFavourite(Integer userId, String htsCode) {
        // retrieve account
        Account account;
        try {
            account = accountRepository.findById(userId).orElseThrow();
        } catch (EntityNotFoundException | NoSuchElementException e) {
            // no such account
            System.out.println(e.getMessage());
            return;
        }

        // add account to favourites
        Favourites favourites;
        try {
            // check if favourite exists
            favourites = favouritesRepository.findById(htsCode).orElseThrow();
            Set<Account> accountsFavourited = favourites.getAccounts();
            accountsFavourited.add(account);

            // update the list of accounts with this item favourited
            favourites.setAccounts(accountsFavourited);
        } catch (EntityNotFoundException | NoSuchElementException e) {
            // create new favourited record if it is new
            favourites = new Favourites(htsCode, Set.of(account));
        }

        // add favourites to account
        account.getFavourites().add(favourites);

        // save changes
        favouritesRepository.save(favourites);
        accountRepository.save(account);
    }

    /**
     * Removes the favourite hts code of user from their account
     * 
     * @param userId  Target user ID
     * @param htsCode Target HTS code
     */
    public void removeFavourite(Integer userId, String htsCode) {
        // retrieve account
        Account account;
        try {
            account = accountRepository.findById(userId).orElseThrow();
        } catch (EntityNotFoundException | NoSuchElementException e) {
            System.out.println(e.getMessage());
            return;
        }

        // remove account from favourite
        Favourites favourites;
        try {
            favourites = favouritesRepository.findById(htsCode).orElseThrow();
            Set<Account> accountsFavourited = favourites.getAccounts();
            accountsFavourited.remove(account);
            favourites.setAccounts(accountsFavourited);
        } catch (EntityNotFoundException | NoSuchElementException e) {
            System.out.println(e.getMessage());
            return;
        }

        // remove favourite from account
        Set<Favourites> favourited = account.getFavourites();
        favourited.remove(favourites);
        account.setFavourites(favourited);

        // save changes
        favouritesRepository.save(favourites);
        accountRepository.save(account);
    }

    /**
     * Retrieves the favourite hts codes of a user from their account
     * 
     * @param userId Target user ID
     * @return Set containing all the favouried HTS codes
     */
    public List<FavouritesDTO> getFavouritesHtsCodes(Integer userId) {
        try {
            return favouritesRepository.findFavouritesByAccountId(userId);
        } catch (EntityNotFoundException | NoSuchElementException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}

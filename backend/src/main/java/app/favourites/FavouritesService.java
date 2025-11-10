package app.favourites;

import org.springframework.stereotype.Service;

import app.account.*;
import app.exception.FavouritesNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class FavouritesService {
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final FavouritesRepository favouritesRepository;

    /**
     * Constructor-based injection.
     * 
     * @param accountRepository Repository dependency.
     */
    public FavouritesService(AccountRepository accountRepository, AccountService accountService,
            FavouritesRepository favouritesRepository) {
        this.accountRepository = accountRepository;
        this.accountService = accountService;
        this.favouritesRepository = favouritesRepository;
    }

    public Favourites getFavouritesByHtsCode(String htsCode) {
        Optional<Favourites> favourites = favouritesRepository.findById(htsCode);

        if (!favourites.isPresent()) {
            throw new FavouritesNotFoundException(htsCode);
        }

        return favourites.get();
    }

    /**
     * Saves the favourite hts code of user into their account
     * 
     * @param userId  Target user ID
     * @param htsCode Target HTS code
     */
    public void addFavourite(Integer userId, String htsCode) {
        // retrieve account
        Account account = accountService.getAccountByUserID(userId);

        // add account to favourites
        Favourites favourites;
        try {
            // check if favourite exists
            favourites = getFavouritesByHtsCode(htsCode);
            Set<Account> accountsFavourited = favourites.getAccounts();
            accountsFavourited.add(account);

            // update the list of accounts with this item favourited
            favourites.setAccounts(accountsFavourited);
        } catch (FavouritesNotFoundException e) {
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
        Account account = accountService.getAccountByUserID(userId);

        // remove account from favourite
        Favourites favourites = getFavouritesByHtsCode(htsCode);
        Set<Account> accountsFavourited = favourites.getAccounts();
        accountsFavourited.remove(account);
        favourites.setAccounts(accountsFavourited);

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
        Optional<List<FavouritesDTO>> favourites = favouritesRepository.findFavouritesByAccountId(userId);

        if (!favourites.isPresent()) {
            throw new FavouritesNotFoundException("No favourites for user with user ID " + userId);
        }

        return favourites.get();
    }
}

package app.favourites;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.account.Account;
import app.account.AccountRepository;
import app.account.AccountService;
import app.exception.FavouritesNotFoundException;
import app.exception.UserNotFoundException;

@ExtendWith(MockitoExtension.class)
public class FavouriteServiceTest {

    @Mock
    private FavouritesRepository favouritesRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private FavouritesService favouriteService;

    private Account account1;
    private Favourites favourites1;

    @BeforeEach
    public void setUp() {
        account1 = new Account();
        account1.setUserID(1);
        account1.setUsername("User1");
        account1.setPassword("password");
        account1.setEmail("user1@test.com");
        account1.setRole("USER");
        account1.setFavourites(new HashSet<>());

        favourites1 = new Favourites();
        favourites1.setHtsCode("1704.01.10");
        favourites1.setAccounts(new HashSet<>());
    }

    // ==================== addFavourite Tests ====================

    @Test
    public void addFavourite_WhenUserAndFavouriteExist_ShouldAddAndSave() {
        when(accountService.getAccountByUserID(1)).thenReturn(account1);
        when(favouritesRepository.findById("1704.01.10")).thenReturn(Optional.of(favourites1));

        favouriteService.addFavourite(1, "1704.01.10");

        verify(accountService).getAccountByUserID(1);
        verify(favouritesRepository).findById("1704.01.10");
        verify(favouritesRepository).save(favourites1);
        verify(accountRepository).save(account1);
        assertTrue(favourites1.getAccounts().contains(account1));
        assertTrue(account1.getFavourites().contains(favourites1));
    }

    @Test
    public void addFavourite_WhenUserExistsButFavouriteDoesNot_ShouldCreateNewFavourite() {
        when(accountService.getAccountByUserID(1)).thenReturn(account1);
        when(favouritesRepository.findById("1704.01.11")).thenReturn(Optional.empty());

        favouriteService.addFavourite(1, "1704.01.11");

        verify(accountService).getAccountByUserID(1);
        verify(favouritesRepository).findById("1704.01.11");
        verify(favouritesRepository).save(any(Favourites.class));
        verify(accountRepository).save(account1);
        assertEquals(1, account1.getFavourites().size());
    }

    @Test
    public void addFavourite_WhenUserDoesNotExist_ShouldThrowUserNotFoundException() {
        when(accountService.getAccountByUserID(2)).thenThrow(new UserNotFoundException("User not found"));

        assertThrows(UserNotFoundException.class, 
            () -> favouriteService.addFavourite(2, "1704.01.10"));

        verify(accountService).getAccountByUserID(2);
        verify(favouritesRepository, never()).findById(anyString());
        verify(favouritesRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    // ==================== removeFavourite Tests ====================

    @Test
    public void removeFavourite_WhenUserAndFavouriteExist_ShouldRemoveAndSave() {
        account1.getFavourites().add(favourites1);
        favourites1.getAccounts().add(account1);

        when(accountService.getAccountByUserID(1)).thenReturn(account1);
        when(favouritesRepository.findById("1704.01.10")).thenReturn(Optional.of(favourites1));

        favouriteService.removeFavourite(1, "1704.01.10");

        verify(accountService).getAccountByUserID(1);
        verify(favouritesRepository).findById("1704.01.10");
        verify(favouritesRepository).save(favourites1);
        verify(accountRepository).save(account1);
        assertFalse(favourites1.getAccounts().contains(account1));
        assertFalse(account1.getFavourites().contains(favourites1));
    }

    @Test
    public void removeFavourite_WhenUserDoesNotExist_ShouldThrowUserNotFoundException() {
        when(accountService.getAccountByUserID(2)).thenThrow(new UserNotFoundException("User not found"));

        assertThrows(UserNotFoundException.class, 
            () -> favouriteService.removeFavourite(2, "1704.01.10"));

        verify(accountService).getAccountByUserID(2);
        verify(favouritesRepository, never()).findById(anyString());
        verify(favouritesRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    public void removeFavourite_WhenFavouriteDoesNotExist_ShouldThrowFavouritesNotFoundException() {
        when(accountService.getAccountByUserID(1)).thenReturn(account1);
        when(favouritesRepository.findById("9999.99.99")).thenReturn(Optional.empty());

        assertThrows(FavouritesNotFoundException.class, 
            () -> favouriteService.removeFavourite(1, "9999.99.99"));

        verify(accountService).getAccountByUserID(1);
        verify(favouritesRepository).findById("9999.99.99");
        verify(favouritesRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    // ==================== getFavouritesHtsCodes Tests ====================

    @Test
    public void getFavouritesHtsCodes_WhenUserHasFavourites_ShouldReturnListOfDTOs() {
        FavouritesDTO dto1 = new FavouritesDTO("1704.01.10", "Chewing gum", "Confectionery", "$0.80", "Free (AU, SG)");
        FavouritesDTO dto2 = new FavouritesDTO("0901.21.00", "Roasted coffee", "Beverages", "$0.45", "Free (AU)");
        List<FavouritesDTO> expectedDTOs = List.of(dto1, dto2);

        when(favouritesRepository.findFavouritesByAccountId(1)).thenReturn(Optional.of(expectedDTOs));

        List<FavouritesDTO> result = favouriteService.getFavouritesHtsCodes(1);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("1704.01.10", result.get(0).htsCode());
        assertEquals("Chewing gum", result.get(0).description());
        assertEquals("0901.21.00", result.get(1).htsCode());
        verify(favouritesRepository).findFavouritesByAccountId(1);
    }

    @Test
    public void getFavouritesHtsCodes_WhenUserHasNoFavourites_ShouldReturnEmptyList() {
        when(favouritesRepository.findFavouritesByAccountId(1)).thenReturn(Optional.of(List.of()));

        List<FavouritesDTO> result = favouriteService.getFavouritesHtsCodes(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(favouritesRepository).findFavouritesByAccountId(1);
    }

    @Test
    public void getFavouritesHtsCodes_WhenUserDoesNotExist_ShouldThrowFavouritesNotFoundException() {
        when(favouritesRepository.findFavouritesByAccountId(2)).thenReturn(Optional.empty());

        FavouritesNotFoundException exception = assertThrows(
            FavouritesNotFoundException.class,
            () -> favouriteService.getFavouritesHtsCodes(2)
        );

        assertEquals("No favourites for user with user ID 2", exception.getMessage());
        verify(favouritesRepository).findFavouritesByAccountId(2);
    }
}
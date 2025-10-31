package app.favourites;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.account.Account;
import app.account.AccountRepository;

@ExtendWith(MockitoExtension.class)
public class FavouriteServiceTest {

    @Mock
    FavouritesRepository favouritesRepository;

    @Mock
    AccountRepository accountRepository;

    @InjectMocks
    FavouritesService favouriteService;

    Account account1;
    Favourites favourites1;

    @BeforeEach
    public void setUp() {
        account1 = new Account(1, "User1", "password", "user1@test.com", new ArrayList<>(), new HashSet<>());
        favourites1 = new Favourites("1704.01.10", new HashSet<>());
    }

    @Test
    public void addFavourite_WhenUserAndFavouritesExists_ShouldSaveFavourite() {
        when(accountRepository.findById(anyInt())).thenReturn(Optional.of(account1));
        when(favouritesRepository.findById(anyString())).thenReturn(Optional.of(favourites1));

        favouriteService.addFavourite(1, "1704.01.10");

        verify(accountRepository).findById(1);
        verify(favouritesRepository).findById("1704.01.10");
        verify(favouritesRepository).save(any(Favourites.class));
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    public void addFavourite_WhenUserDoesNotExist_ShouldExit() {
        when(accountRepository.findById(anyInt())).thenReturn(Optional.empty());

        favouriteService.addFavourite(2, "1704.01.10");

        verify(favouritesRepository, never()).findById(anyString());
        verify(favouritesRepository, never()).save(any(Favourites.class));
        verify(accountRepository, never()).save(any(Account.class));
        verify(accountRepository).findById(2);
    }

    @Test
    public void addFavourite_WhenUserExistsButFavouritesDoesNot_ShouldSaveFavourite() {
        when(accountRepository.findById(anyInt())).thenReturn(Optional.of(account1));
        when(favouritesRepository.findById(anyString())).thenReturn(Optional.empty());

        favouriteService.addFavourite(1, "1704.01.11");

        verify(accountRepository).findById(1);
        verify(favouritesRepository).findById("1704.01.11");
        verify(accountRepository).save(any(Account.class));
        verify(favouritesRepository).save(any(Favourites.class));
    }

    @Test
    public void removeFavourite_WhenUserAndFavouriteExists_ShouldRemoveAndSave() {
        account1.getFavourites().add(favourites1);
        favourites1.getAccounts().add(account1);

        when(accountRepository.findById(anyInt())).thenReturn(Optional.of(account1));
        when(favouritesRepository.findById(anyString())).thenReturn(Optional.of(favourites1));

        favouriteService.removeFavourite(1, "1704.01.10");

        verify(accountRepository).findById(1);
        verify(favouritesRepository).findById("1704.01.10");
        verify(favouritesRepository).save(any(Favourites.class));
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    public void removeFavourite_WhenUserDoesNotExist_ShouldExitWithoutSaves() {
        when(accountRepository.findById(anyInt())).thenReturn(Optional.empty());

        favouriteService.removeFavourite(2, "1704.01.10");

        verify(favouritesRepository, never()).findById(anyString());
        verify(favouritesRepository, never()).save(any(Favourites.class));
        verify(accountRepository, never()).save(any(Account.class));
        verify(accountRepository).findById(2);
    }

    @Test
    public void removeFavourite_WhenFavouriteDoesNotExist_ShouldExitWithoutSaves() {
        when(accountRepository.findById(anyInt())).thenReturn(Optional.of(account1));
        when(favouritesRepository.findById(anyString())).thenReturn(Optional.empty());

        favouriteService.removeFavourite(1, "9999.99.99");

        verify(accountRepository).findById(1);
        verify(favouritesRepository).findById("9999.99.99");
        verify(favouritesRepository, never()).save(any(Favourites.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void getFavouritesHtsCodes_WhenUserExists_ShouldReturnSetOfCodes() {
        account1.getFavourites().add(favourites1);
        when(accountRepository.findById(anyInt())).thenReturn(Optional.of(account1));

        Set<String> codes = favouriteService.getFavouritesHtsCodes(1);

        assertNotNull(codes);
        assertEquals(1, codes.size());
        assertTrue(codes.contains("1704.01.10"));
    }

    @Test
    public void getFavouritesHtsCodes_WhenUserDoesNotExist_ShouldReturnNull() {
        when(accountRepository.findById(anyInt())).thenReturn(Optional.empty());

        Set<String> codes = favouriteService.getFavouritesHtsCodes(2);

        assertNull(codes);
        verify(accountRepository).findById(2);
    }

    @Test
    public void getFavouriteHtsCodes_WhenUserExistsAndNoFavourites_ShouldReturnEmptySet() {
        when(accountRepository.findById(anyInt())).thenReturn(Optional.of(account1));

        Set<String> codes = favouriteService.getFavouritesHtsCodes(1);

        assertNotNull(codes);
        assertTrue(codes.isEmpty());
        verify(accountRepository).findById(1);
    }
}

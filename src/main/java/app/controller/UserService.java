package app.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserService {
    // This is just a sample list, replace it with a database connection or repository in a real app
    private List<User> users = new ArrayList<>();

    // Get all users
    public List<User> getAllUsers() {
        return users;
    }

    // Get a user by their ID
    public Optional<User> getUserById(Long id) {
        return users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }

    // Add a new user
    public void addUser(User user) {
        users.add(user);
    }

    // Update an existing user
    public boolean updateUser(Long id, User updatedUser) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(id)) {
                users.set(i, updatedUser);
                return true;
            }
        }
        return false;
    }

    // Remove a user by their ID
    public boolean deleteUser(Long id) {
        return users.removeIf(user -> user.getId().equals(id));
    }
}

package puc.airtrack.airtrack;

import org.springframework.stereotype.Repository;

import puc.airtrack.airtrack.Login.User;

import org.springframework.data.jpa.repository.*;


@Repository
public interface Repositorio extends JpaRepository<User, Integer> {
    User findByUsername(String username);
    User findById(int id);
    User findByUsernameAndPassword(String username, String password);
    User findByIdAndStatus(int id, Boolean status);
    User findByIdAndRole(int id , Boolean role);
}

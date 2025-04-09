package puc.airtrack.airtrack.Login;

import org.springframework.beans.factory.annotation.Autowired;

import puc.airtrack.airtrack.Repositorio;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private Repositorio repositorio;

    public UserService(Repositorio repositorio) {
        this.repositorio = repositorio;
    }

    public User findByUsername(String username) {
        return repositorio.findByUsername(username);
    }

    public User findById(int id) {
        return repositorio.findById(id);
    }

    public User findByUsernameAndPassword(String username, String password) {
        return repositorio.findByUsernameAndPassword(username, password);
    }
    public void save(User user) {
        repositorio.save(user);
    }
    
    public User findByIdAndStatus(int id, Boolean status) {
        return repositorio.findByIdAndStatus(id, status);
    }

    public User findByIdAndRole(int id, Boolean role) {
        return repositorio.findByIdAndRole(id, role);
    }
}

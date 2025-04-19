package puc.airtrack.airtrack.Login;

public enum UserRole {
    ADMIN(4),
    ENGENHEIRO(3),
    SUPERVISOR(2),
    AUDITOR(1);

    private int role;

    UserRole(int role) {
        this.role = role;
    }

    public int getRole() {
        return role;
    }
}

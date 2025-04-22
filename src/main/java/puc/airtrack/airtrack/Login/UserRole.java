package puc.airtrack.airtrack.Login;

public enum UserRole {
    ENGENHEIRO(1),
    AUDITOR(2),
    SUPERVISOR(3),
    ADMIN(4);

    private int role;

    UserRole(int role) {
        this.role = role;
    }

    public int getRole() {
        return role;
    }

    public static UserRole fromRoleValue(int roleValue) {
        for (UserRole role : UserRole.values()) {
            if (role.getRole() == roleValue) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role value: " + roleValue);
    }
}

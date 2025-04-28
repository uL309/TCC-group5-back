package puc.airtrack.airtrack.Login;

public enum UserRole {
    ROLE_ENGENHEIRO(0),
    ROLE_AUDITOR(1),
    ROLE_SUPERVISOR(2),
    ROLE_ADMIN(3);

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

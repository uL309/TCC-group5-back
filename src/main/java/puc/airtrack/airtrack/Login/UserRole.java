package puc.airtrack.airtrack.Login;

public enum UserRole {
    ROLE_ENGENHEIRO(1),
    ROLE_AUDITOR(2),
    ROLE_SUPERVISOR(3),
    ROLE_ADMIN(4);

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

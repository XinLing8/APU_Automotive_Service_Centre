package logic;

import models.User;

public class UserAuthorization {

    public static boolean canCreate(User user, String targetRole) {
        if (user.getRole().equals("Manager")) {
            return targetRole.equals("Manager") || targetRole.equals("Technician") || targetRole.equals("Counter Staff");
        }

        if (user.getRole().equals("Counter Staff")) {
            return targetRole.equals("Customer");
        }
        return false;
    }

    public static boolean canUpdate(User user, String targetRole) {
        return canCreate(user, targetRole);
    }

    public static boolean canDelete(User user, String targetRole) {
        return canCreate(user, targetRole);
    }
}
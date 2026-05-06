package de.sosec.wicketads;

import de.sosec.wicketads.model.User;
import de.sosec.wicketads.service.UserService;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;

public class WicketAdsSession extends AuthenticatedWebSession {
    private static final long serialVersionUID = 1L;

    private User currentUser;

    public WicketAdsSession(Request request) {
        super(request);
    }

    @Override
    public boolean authenticate(String username, String password) {
        User user = UserService.findByUsername(username);
        if (user != null && password.equals(user.getPassword())) {
            currentUser = user;
            return true;
        }
        return false;
    }

    @Override
    public Roles getRoles() {
        if (isSignedIn() && currentUser != null) {
            return new Roles(currentUser.getRole());
        }
        return new Roles();
    }

    @Override
    public void signOut() {
        currentUser = null;
        super.signOut();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return isSignedIn() && currentUser != null;
    }

    public boolean isAdmin() {
        return isLoggedIn() && "admin".equals(currentUser.getRole());
    }

    public static WicketAdsSession get() {
        return (WicketAdsSession) AuthenticatedWebSession.get();
    }
}

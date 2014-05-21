package com.bravson.socialalert.components;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SessionState;

import com.bravson.socialalert.common.domain.UserInfo;
import com.bravson.socialalert.common.domain.UserRole;

public class IfRole {

    /**
     * A comma-separated list of roles is supplied to one or more of the
     * following parameters. If none are supplied, the default behavior is to
     * permit access. Behavior should be self-explanatory.
     */
    @Parameter(required = false, defaultPrefix = "literal")
    private String allow;

    @Parameter(required = false, defaultPrefix = "literal")
    private String disallow;


    /**
     * An alternate {@link Block} to render if the ok parameter is false. The default, null, means
     * render nothing in that situation.
     */
    @Parameter(name = "else")
    private Block elseBlock;

    private boolean ok;
    
    @SessionState(create=false)
    private UserInfo userInfo;
    
    void setupRender() {
        ok = UserRole.checkPermission(userInfo, UserRole.parseRoles(allow), UserRole.parseRoles(disallow));
    }

    /**
     * Returns null if the ok method returns true, which allows normal
     * rendering (of the body). If the ok parameter is false, returns the else
     * parameter (this may also be null).
     */
    Object beginRender() {
        return ok ? null : elseBlock;
    }

    /**
     * If the ok method returns true, then the body is rendered, otherwise not. The component does
     * not have a template or do any other rendering besides its body.
     */
    boolean beforeRenderBody() {
        return ok;
    }
}

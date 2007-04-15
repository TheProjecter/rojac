package org.xblackcat.sunaj.service.janus.data;

import org.xblackcat.sunaj.service.data.PostException;
import ru.rsdn.Janus.PostExceptionInfo;

/**
 * Date: 14.04.2007
 *
 * @author ASUS
 */

public final class PostInfo {
    private final int[] commited;
    private final PostException[] exceptions;

    public PostInfo(int[] commited, PostExceptionInfo[] exceptions) {
        this.commited = commited;
        
        this.exceptions = new PostException[exceptions.length];
        for (int i = 0; i < exceptions.length; i++) {
            this.exceptions[i] = new PostException(exceptions[i]);
        }
    }

    public int[] getCommited() {
        return commited;
    }

    public PostException[] getExceptions() {
        return exceptions;
    }
}

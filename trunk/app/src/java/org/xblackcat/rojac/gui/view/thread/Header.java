package org.xblackcat.rojac.gui.view.thread;

import org.xblackcat.rojac.RojacException;
import org.xblackcat.rojac.i18n.Messages;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
* @author xBlackCat
*/
enum Header {
    ID(PostId.class, Messages.Panel_Thread_Header_Id, 70),
    SUBJECT(Post.class, Messages.Panel_Thread_Header_Subject, 0),
    USER(PostUser.class, Messages.Panel_Thread_Header_User, 100),
    REPLIES(PostReplies.class, Messages.Panel_Thread_Header_Replies, 30),
    RATING(PostRating.class, Messages.Panel_Thread_Header_Rating, 70),
    DATE(PostDate.class, Messages.Panel_Thread_Header_Date, 140);

    private final Class<?> aClass;
    private final Constructor<?> constructor;
    private final Messages title;
    private final int width;

    Header(Class<?> aClass, Messages title, int width) {
        this.aClass = aClass;
        this.title = title;
        this.width = width;
        if (aClass.getSuperclass() == APostProxy.class) {
            try {
                this.constructor = aClass.getConstructor(Post.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Can not prepare convertor for class " + aClass.getName(), e);
            }
        } else {
            this.constructor = null;
        }
    }

    public Class<?> getObjectClass() {
        return aClass;
    }

    public Object getObjectData(Object o) throws RojacException {
        if (constructor != null && o instanceof Post) {
            try {
                return constructor.newInstance(o);
            } catch (InstantiationException e) {
                throw new RojacException("Can not initialize data class " + aClass.getName(), e);
            } catch (IllegalAccessException e) {
                throw new RojacException("Security check not passed for data class " + aClass.getName(), e);
            } catch (InvocationTargetException e) {
                throw new RojacException("Can not initialize data class " + aClass.getName(), e);
            }
        } else {
            return o;
        }
    }

    public String getTitle() {
        return title.get();
    }

    public int getWidth() {
        return width;
    }
}

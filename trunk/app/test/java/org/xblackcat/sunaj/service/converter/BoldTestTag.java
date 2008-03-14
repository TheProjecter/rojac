package org.xblackcat.sunaj.service.converter;

/**
 * Date: 20 ��� 2008
 *
 * @author xBlackCat
 */

public class BoldTestTag implements ITag<BoldTestTag> {
    private static final String OPEN_TAG = "[b]";
    private static final String CLOSE_TAG = "[/b]";

    public ITagInfo find(final String text, final String lower) {
        final int startPos = lower.indexOf(OPEN_TAG);

        if (startPos == -1) {
            return null;
        }

        return new ITagInfo() {
            public int start() {
                return startPos;
            }

            public ITag getTag() {
                return BoldTestTag.this;
            }

            public ITagData process() {
                final int endPos = lower.indexOf(CLOSE_TAG, startPos);

                if (endPos == -1) {
                    return null;
                }

                return new ITagData() {
                    public int start() {
                        return startPos;
                    }

                    public int end() {
                        return endPos + CLOSE_TAG.length();
                    }

                    public String getHead() {
                        return "<b>";
                    }

                    public String getTail() {
                        return "</b>";
                    }

                    public String getBody() {
                        return text.substring(startPos + OPEN_TAG.length(), endPos);
                    }
                };
            }
        };
    }

    public int compareTo(BoldTestTag o) {
        return 0;
    }
}

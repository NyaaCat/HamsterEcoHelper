package cat.nyaa.heh.business.item;

import cat.nyaa.heh.utils.MessagedThrowable;
import cat.nyaa.nyaacore.Message;

public class StorageSpaceException extends MessagedThrowable {
    public StorageSpaceException(Message message) {
        super(message);
    }
}

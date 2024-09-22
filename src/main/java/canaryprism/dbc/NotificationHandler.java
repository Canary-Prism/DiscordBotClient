package canaryprism.dbc;

import java.util.HashSet;
import java.util.Set;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;

public class NotificationHandler {

    private static final Set<Channel> unread_channels = new HashSet<>();

    public void submitApi(DiscordApi api) {
        api.addMessageCreateListener((e) -> {
            unread_channels.add(e.getChannel());
        });
    }

    public void markAsRead(Channel channel) {
        unread_channels.remove(channel);
    }

    public boolean hasUnread(Channel channel) {
        return unread_channels.contains(channel);
    }
}

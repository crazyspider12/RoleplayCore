package plugins.astro.roleplaycore;

import org.bukkit.entity.Player;

public class Cooldown {
	private Player player;
	private Channel channel;
	private int secondsLeft;

	public Cooldown(Player player, Channel channel) {
		this.player = player;
		this.channel = channel;
		this.secondsLeft = channel.getCooldown();
	}

	public void decrementSeconds() {
		secondsLeft--;
	}

	public Player getPlayer() {
		return player;
	}

	public Channel getChannel() {
		return channel;
	}

	public int getSecondsLeft() {
		return secondsLeft;
	}
}

# Speedrun Race

This plugin lets you run team-based speedrun races.
It depends on LuckPerms and Chunky.

**User Guide**

Use `/race pregen` to create the following folders:

- `template_overworld` for the overworld map
- `template_nether` for the nether map
- `template_end` for the end map

Put your world files in these folders to use the seed you want. Otherwise, the worlds will be generated randomly.

To create teams, create groups with LuckPerms. Once the groups exist, players can join one with `/join [group-name]`.
This lets you manage teams from the LuckPerms dashboard.
When the pregen has finished and teams are ready, run `/race duplicate` as an admin to copy and load each team's worlds before the race.
Then run `/race start`; players will be teleported, can move during the 10-second countdown, and the timer starts after the countdown.

This project uses the MiniMessage API and is still under development. Contributions are welcome.


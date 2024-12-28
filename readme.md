# DevMMO (Minecraft Plugin)
This plugin is used on Developcraft Server (mc-developcraft.net) 
and is a custom made plugin aims to make server configuration more easier and also add some new features to the server

## Features
- Damage Tracker
  - Track the damage dealt by players
  - currently has no use case (future will be boss damage destribution system)
- Health Fixer
  - Fix the problem where player rejoin the server and sometimes the health got reset to default value (20 hp)
- Firework Blocker
  - Block the firework from being used in the certain world to prevent lag (like in MMO world)
- Mob EXP
  - Automatically give player exp when they kill a mob where amount of exp given is based on the configurable math formula (aka level based)
- EXP Share
  - Destribute the exp to all players who dealt damage to the mob when the mob is dead
- Region Level 
  - Used with MythicMob's random spawn feature, automatically assign the level of the mob based on specific worldguard region 
- Stamina System (Running System)
  - Make MMOCore's stamina system more useful by implementing the running system where the player's stamina will decrease when they are running, they can get exhausted and can't run anymore

## Commands
- /devmmo reload
  - Reload the plugin configuration
- /smm <mob> <level> [amount]
  - alternatives command for /mm m spawn, this command can summon a mythic mob with specific level and amount

## Installation
1. Compile this plugin using `gradle build`
2. Copy the jar file from `build/libs` to your server's plugin folder
3. Start the server
4. Configure the plugin's module in the `modules.yml` file, all modules are disabled by default
5. Configure the plugin's module configuration to match your server's need
6. Reload the plugin using `/devmmo reload`

## Dependencies
- MythicLib
- MythicMobs
- MMOCore
- WorldGuard

## Contributing
1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request.

## License

The MIT License (MIT)

Copyright (c) 2015 Chris Kibble

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
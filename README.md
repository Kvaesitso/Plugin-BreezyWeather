# Kvaesitso Breezy Weather plugin

This plugin is a weather provider plugin for [Kvaesitso](https://kvaesitso.mm20.de)
that makes use of [Breezy Weather's](https://github.com/breezy-weather/breezy-weather) data sharing
feature and forwards the data to Kvaesitso.

## Setup

First, install the plugin. You can either download it from the [releases page](https://github.com/Kvaesitso/Plugin-BreezyWeather/releases), or
from the [MM20 F-Droid repository](https://fdroid.mm20.de/).

Then, you need to enable data sharing in Breezy Weather. To do this, open Breezy Weather, go to
Settings > Widget & Live wallpaper > Send Gadgetbridge data, and enable the plugin.

Finally, open Kvaesitso, go to Settings > Plugins, enable the Breezy Weather plugin, and set it
as weather provider.

## Known issues

Kvaesitso only updates its weather data every 60 minutes. When you change your location or provider
settings in Breezy Weather, it may take up to 60 minutes for these changes to arrive in Kvaesitso.
To force an update, you can temporarily switch to another weather provider in Kvaesitso, and then
switch back to Breezy Weather.

## License

This plugin is licensed under the Apache License 2.0.

```
Copyright 2023 MM2-0 and the Kvaesitso contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

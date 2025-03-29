# Siona: Chronosphere

A Minecraft mod that integrates TARDIM, ComputerCraft and more to create an immersive technological experience around time and space manipulation.

## 🌟 Current Features

### TARDIM Integration
- **TARDIM Control Interface**: A peripheral block that interfaces with TARDIM systems when placed inside a TARDIS
- **ComputerCraft API**: Control your TARDIS programmatically using Lua scripts
- **Remote Control**: Manage flight, navigation, and TARDIS systems without manual interaction

### ComputerCraft API Functions

The TARDIM peripheral provides the following functions:

#### Information Methods
- `getFuel()` - Returns the current fuel level of the TARDIM (0-100)
- `calculateFuelForJourney()` - Calculates fuel needed for the currently set destination
- `isLocked()` - Returns whether the TARDIM is locked
- `isInFlight()` - Returns whether the TARDIM is in flight
- `getTimeEnteredFlight()` - Returns timestamp when flight began or -1 if not in flight
- `getOwnerName()` - Returns name of the TARDIM's owner
- `getCurrentLocation()` - Returns a table with current location details
- `getTravelLocation()` - Returns a table with destination details
- `getCompanions()` - Returns a list of companion usernames

#### Control Methods
- `setLocked(boolean)` - Lock or unlock the TARDIM
- `setDimension(string)` - Set destination dimension (e.g., "minecraft:overworld")
- `setTravelLocation(x, y, z)` - Set destination coordinates
- `setDoorRotation(string)` - Set door rotation ("north", "south", "east", "west")
- `getDoorRotation()` - Get the current door rotation
- `toggleDoorRotation()` - Cycle through door rotation options
- `coordAdd(axis, amount)` - Add to destination coordinates on specified axis

#### Travel Methods
- `demat()` - Dematerialize the TARDIM (take off)
- `remat()` - Materialize the TARDIM at the destination (land)
- `home()` - Set destination to the TARDIM owner's spawn point
- `locatePlayer(username)` - Set destination to a specific player's location
- `locateBiome(biome_id)` - Find and set destination to specified biome

#### Information Gathering
- `getOnlinePlayers()` - Get a list of online player names
- `getSkins()` - Get a list of available TARDIM skins
- `getBiomes()` - Get a list of all biome IDs
- `getDimensions()` - Get a list of all dimension IDs

### Example Usage

```lua
-- Connect to the peripheral
local tardim = peripheral.find("tardim")

-- Check fuel levels
local fuel = tardim.getFuel()
print("Current fuel: " .. fuel .. "%")

-- Set destination
tardim.setDimension("minecraft:overworld")
tardim.setTravelLocation(100, 64, 200)

-- Take off
if not tardim.isInFlight() then
  tardim.demat()
  print("TARDIS taking off")
else
  print("Already in flight")
end

-- Wait a bit, then land
sleep(15)
if tardim.isInFlight() then
  tardim.remat()
  print("TARDIS landing")
end
```

## 🛠️ Installation

1. Install Minecraft Forge for version 1.20.1
2. Install required dependencies:
   - ComputerCraft: Tweaked
   - TARDIM
   - Curios API (Forge)
3. Place the Siona-Chronosphere-.jar file in your "mods" folder
4. Launch the game

## 📝 Configuration

The mod includes configuration options you can adjust in:
- `config/sionacs-common.toml` - Server-side settings
- `config/sionacs-client.toml` - Client-side settings

## 🔮 Planned Features

### Implant System
- **Neural Implants**: Interface directly with the player's nervous system
- **Kinetic Implants**: Enhance movement capabilities
- **Optical Implants**: Provide visual overlays and enhancements

### Tetra Integration
- **Chronosphere Materials**: Special materials for crafting Tetra tools
- **Temporal Enchantments**: Time-manipulating holo-enchantments
- **Tool Peripherals**: Connect Tetra tools to computer systems

### Expanded TARDIS Controls
- **Enhanced visualization**: Better TARDIM interior-exterior transition using Immersive Portals
- **Automated systems**: More sophisticated computerized control options
- **Emergency protocols**: Automated safety features

### Chronosphere Dimension
- A unique dimension accessible via modified TARDIM technology
- Special resources and crafting opportunities

## 📋 Dependencies

- **Minecraft Forge**: 1.20.1
- **ComputerCraft: Tweaked**: Required
- **TARDIM**: Required
- **Curios API**: Required
- **Tetra**: Planned/Optional
- **Immersive Portals**: Planned/Optional

## 📚 Wiki & Documentation

More detailed documentation is coming soon!

## 🐛 Bug Reports & Feature Requests

Please report bugs and suggest features via GitHub issues.

---

Made with 💙 by Xylopia

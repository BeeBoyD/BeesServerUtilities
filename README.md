# 🐝 Bee's Server Utilities  
*A powerful yet lightweight utility mod for Minecraft servers*  

[![Modrinth](https://img.shields.io/modrinth/dt/beeserverutilities?logo=modrinth&color=5da545)](https://modrinth.com/mod/beesserverutilities)
[![License](https://img.shields.io/github/license/YOUR_GITHUB/beeserverutilities)](https://github.com/YOUR_GITHUB/BeesServerUtilities/blob/forge-1.19.4/LICENSE)
[![Discord](https://img.shields.io/discord/YOUR_DISCORD_ID?label=Discord&logo=discord&color=7289DA)](https://discord.gg/YOUR_INVITE)

---

## ✨ Features  
**Bee Server Utilities** is a must-have for server administrators, offering powerful automation tools and quality-of-life features, including:  

✅ **Auto-Executing Commands** – Run commands automatically on player join, leave, server startup, shutdown, or time intervals.  
✅ **Flexible Scheduling** – Supports commands at **custom intervals**, on **specific players**, or globally.  
✅ **Autocomplete & GUI** – In-game `/autoexec` command with **smart autocompletion** and an intuitive GUI.  
✅ **Persistent Storage** – Autoexec rules are **saved to JSON** and persist across restarts.  
✅ **Cross-Compatibility** – Works seamlessly on **Forge 1.19.4+** and **Fabric (coming soon!)**  

---

## 📦 Installation  
1. **Download the mod** from [Modrinth](https://modrinth.com/mod/beeserverutilities) or [GitHub Releases](https://github.com/YOUR_GITHUB/beeserverutilities/releases).  
2. **Install Forge** for your Minecraft server.  
3. **Place the `.jar` file** into your `mods/` folder.  
4. **Restart your server** and configure the mod to your needs!  

---

## 📝 Commands  

### **`/autoexec add <name> <scheduleType> <target> <command>`**  
Registers a new auto-executing command.  

#### **Example Usage:**  
```mcfunction
/autoexec add welcomeMessage ON_PLAYER_JOIN any /say Welcome to the server!

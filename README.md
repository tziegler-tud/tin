# approximateQueries
Repo for my research project on regular path queries.

---

Preliminaries for Windows
- wsl (to run redis-server)
- DBeaver or similar db management system (optional)
- Frontend: https://gitlab.tcs.inf.tu-dresden.de/s8880157/tintheweb

---
Setup 

1. Clone Tin and TinTheWeb Repos into one parent folder, e.g. /dev
2. Copy application.default.properties to application.properties and change settings according to your setup
3. Set up configurations similar to this: 

<details>
<summary>Click to expand</summary>
   Note: You may exclude the gradle tasks bootJar and publishToMavenLocal from this, but be sure to run these tasks whenever you changed Controllers or DataClasses (else the Typescript Generator will fail due to an outdated jar file)!

![Example Configuration Tin](src/main/resources/example_run_config_tin.png)

</details>

4. Run Ubuntu for Windows, inside the cmd-prompt, run "redis-server"
5. Run tin, if you didn't change the port it's running on port 8900.
6. Set up Tintheweb according to its ReadMe for a graphical UI.


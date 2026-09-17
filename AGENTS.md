# Agent instructions

## Serialize Java execution

- Only one model or agent may run Java-related commands at a time, including the lead agent and all helper heads, across project copies.
- This includes Gradle builds, tests, Java tools, and client/startup tasks. Do not delegate these to concurrent heads.
- The lead must designate a single Java runner. Other heads may read and edit files in parallel, but must hand their Java validation commands to that runner.
- Wait for the current runner to finish and report before another model starts Java work. If ownership is unclear, do not launch Java.
- This coordination rule does not authorize in-game or client testing that is otherwise prohibited.

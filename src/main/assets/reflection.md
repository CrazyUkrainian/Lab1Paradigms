This To-Do app was a solid exercise in applying functional programming principles. 
Refactoring to use immutable data made the code cleaner and easier to work with,
and organizing it into layers—UI, logic, and data access—really helped simplify both testing and debugging.

Kotlin’s map and filter methods replaced repetitive loops, making the code more concise and readable. 
Adding a validation function for task numbers reduced duplication and streamlined input handling. 
Implementing the undo feature was challenging but rewarding,
as it pushed me to think more critically about tracking and reverting state changes.

Why This Code is Fully Immutable:
No Shared State - The application's state is only managed within main, and every function explicitly receives and returns the state instead of modifying a global variable.
Encapsulation -  All updates to tasks and lastAction occur through the immutable TodoAppState.
Functional Approach -  Each function operates independently and does not rely on or mutate external/global variables.
Thread Safety - Since there’s no mutable shared state, this design inherently avoids concurrency issues.

Creating the call graphs before and after the refactor was a useful visual tool ,
it showed how much the structure improved. 
Overall, this project highlighted the importance of writing clear and maintainable code,
while reinforcing the practical benefits of functional programming.

*I used AI with one chat, but it has image which doesn't allow me to share it. However, that chat still
was reliably small, I just wanted to clarify there my diagram part, and I asked it to check my final work.
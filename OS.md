It has been brought to my attention that I keep using OS related terms and acronyms without always making sure that you all 
know what I am talking about.  Since we are not planning to use TI-RTOS I have decided to send out at least a high level 
overview of what an OS is.

Software Layers
===============
So to begin with an OS is really a high level abstracted concept rather than one thing that you can easily point to.  
An OS is made up of different "layers" of code.  One way to think of layers is how language works.  You have 26 letters 
at the very bottom.  We then can put together those 26 letters into millions of words which make a second layer.  At this 
point we have some meaning from the words, but not really any concepts.  On a third layer the words are combined with 
punctuation to create complex thoughts and ideas.  Those thoughts and ideas can then be combined together into books and 
stories which can then be combined together into libraries.  Using the analogy the C programming language (or assembly) 
and hardware are the letters.  The operating system kernel are the words, the operating system drivers and traps are the 
punctuation, and the thoughts are the system APIs (like the C standard library).  Above that are then the complex 
libraries like eGLS and CUrL, and the libraries are the actual programs and applications that you use on a computer.

Pieces OS?
==============
So lets start breaking this down.  At its simplest definition an OS is software that abstracts the high level software application from the low level hardware that it is running on.  At the lowest level of the OS is the kernel.  The kernel is where all the magic happens in an operating system and includes the core components: memory management, driver management, context management, and system management.  I will break down each of these in a moment, but first lets talk about what the kernel is.

The OS Kernel
-------------
### Startup
The kernel is a program usually written in C.  When you write a program on a computer you start with `int main(int argc, char** argv)` which is the entry point of the program.  In the kernel the entry point is the start-up vector of the processor.  When the processor first turns on it will start looking for code at a certain address in memory (0x1000 for example).  The Kernel will be compiled in such a way that its initialization code will be located at that very specific address.  The initialization code will setup the kernel components and low level hardware such as system tick timers, power management hardware, and interrupt vectors.  After initialization the kernel will initialize all of the driver system in the HAL layer.  The last thing the kernel does during startup is drop into the scheduler which will switch to user mode.

### Context Management
Context is the state of the processor at any given point in time.  The context includes the values in each of the registers, the contents of the stack, the program counter, the link register, and other processor specific elements.  The most important thing about context is that replecating the a context will always have the same result.  In other words, if I copied the context at any point in time and let the system keep running, later if I set the context to what I previously copied the program would start executing again from where the context was copied and give the exact same output.  

#### Context Switch
In a single core system only one context can run at a given time.  This means that only one program can use the processor at any given time, but we would like to have multiple programs running on a system at the same time.  The solution is time sharing or time slicing: give each context a short amount of time to use the processor and then save that context and restore a different context.  Since when the context is restored the program will keep running with no way of knowing it was interrupted this creates the illusion of multiple programs running concurently.  Multi-core systems work the same way, but allow on process to run per core at any given time.

#### Scheduler
The scheduler is a part of the kernel's context managment system that decides which context (or thread) to run next.  Usually the scheduler will use a system tick (a periodic timer interrupt) to control the length of time each context is allowed to run.  The simplest scheduler will give each context an equal amount of time to run called a quantum.  After that context has run its quantum the next will run and this is repeated until all contexts have run.  The scheduler then starts over with the first context.  This is call Round Robbin Scheduling.  Other more complex schedulers take into account priority and other factors to create a more fair system.

### Memory Management
When a system has multiple processes running at the same time care must be taken to ensure that each process doesn't effect the other processes on the system.  The memory managment unit (MMU) is responsible for protected memory for each process.  The simplest solution for memory management is to give each process a block of memory and trust that it will stay in that area; this is how most embedded operating systems work.  More complex systems will use virtual memory that maps the addresses the programs use to real address in memory.  For example, program A might have a pointer to address `0x0100A4`, but the real location of that memory is at address `0x1300A4`.  MMU does this conversion without the program or programmer ever needing to worry about it.

#### DMA
The other important aspect of the memory management system is controlling the direct memory access (DMA) hardware.  The DMA is a seperate peice of hardware outside the processor that can move memory from one location to another.  The processor sets up the DMA telling it "move 1kB from address `0x10000` to address `0xA0000`" and then can go onto other things while this memory transfer happens in the background.

### Driver Management
The last component of the kernel is driver management.  Drivers are something that are outside of the kernel, but they must be managed and loaded by the kernel.  For example, a UART driver recieves new information which causes an interrupt.  That interrupt will go to the kernel first and then the kernel will schedule time for the UART driver to retrieve the recieved information.  Another case is the user wants to transmit some data though UART so it will send the data to the kernel which knows to use the UART driver to do the transmittion.

### Kernel Traps
Once the kernel has finished initialzation it switch to user level code.  At that point the kernel code is no longer executing and will not execute again until a trap occures.  A trap occurs when the user program needs the kernel for something.  For example, program A wants to send data to the UART driver.  It will first `open` the UART driver "file", `write` the data to the "file", and then `close` the "file".  In this example `open`, `write`, and `close` are all system calls which will cause a kernel trap.  When the user compiles their code for a certain OS they link agains that OS's standard library, `stdio.h`.  When the program calls these system calls the compiler will call functions in the kernel instead of in the users program.  This is a complex operation, but simply put those calls are directly call code in the kernel.  The kernel will then handel the request, call drivers if needed, and then if possible return to the calling process.  

It may occure that a system call cannot be completed in a quick amount of time.  For example, program B requests the the current analog value on an ADC pin but program C is currently using the ADC.  In this case program B will block.  It will be put into a queue to wait for the ADC and then the scheduler will choose a different thread to run.  Later when the ADC because free program B will become runnable (or ready) again.  After it has become runnable eventually the scheduler will get back to it and run that program again.

Drivers
-------
The drivers are part of the hardware abstraction layer (HAL).  This layer is meant to abstract the low level registers and hardware from the user.  For example, a UART driver would apear to the application as a simple file that can be read from and written to.  In reality it is a complex stream which BAUD rate, flow control, and interrupt handlers.  This level of abstraction can greatly simplify application code and make software more portable.  Drivers do not have single entry points like normal software or even a kernel.  Instead they can have any number of functions that the kernel is able to call on the driver when system calls occure.  In the UART example there might be functions for open, close, read, and write.  Each of these would be a function that the user code can call indirectly though the correct system calls.

Processes
---------
Processes, threads, or tasks depending on the system is the componets that actually run the application code.  Processes are the code that runs in the user level above the OS.  They have little to now low level access to the system and must get all access to the hardware through the operating system.  In embedded systems with limited resources this restriction is not as tight and often times the process can directly access the hardware although it rarely should.

System Libraries
----------------
System libraries are the software libraries that the processes should link agains.  It is how the compiler will set up the correct kernel traps to system calls.  It also defines how the kernel will start a program.  For example, a C program starts with `int main(int argc, char ** argv)`.  The kernel is responsible for setting up the `argc` and `argv` parameters and starting the `main` function.  The way that this occurs is defined in the system library.

Unique Aspects of an Embedded OS
================================
ToDo

TI-RTOS
=======
ToDo


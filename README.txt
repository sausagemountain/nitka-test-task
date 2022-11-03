The server is exposing its default port: 8080
The request to process a file should be sent to url `/process/{n}` where n is the number of consumer threads
This should be a multipart request, where the file should be stored in a `file` field

Upon receiving the request a starting message is printed.
After that each line is read and for each line a task is created. (that is the producer)
The task is then associated with the id of the message and put in a queue.
At the same time a queue handler is checking if there are tasks that could be scheduled for running. (that is the checking loop)
    (a task can be scheduled if there is no running task with the same id)
When a task is scheduled it is run as soon as there is a `free` (not executing anything) thread available.
After the file is completely read the producer adds an "end task" (distinct from any other regular task) to the handler, which will be executed after the handler exits its main cheking loop.
Following that the producer sends a message to the handler, indicating that the execution should be stopped when there are no regular tasks left (except the "end task")
    (until such a message is sent the handler will run the checking loop indefinitely)
When the checking loop stops the end task (printing an ending message) is executed.

For actually running regular tasks and printing results a standard java  `ExecutorService` is used, with the same size thread pool as the number of consumers in the request.
The producer and the checking loop are run in their own separate threads.
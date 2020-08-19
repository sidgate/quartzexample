package com.example.quartzexample

import org.quartz.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import kotlin.random.Random

@Entity
data class Task(@Id val id: Int? = null, val name: String, val scheduletime: Instant? = Instant.now())

interface TaskRepository : JpaRepository<Task, Int>

@RestController
class TaskService(val taskRepository: TaskRepository, val scheduler: Scheduler) {


    @PostMapping("/tasks")
    fun createTask(@RequestBody taskRequest: TaskRequest) {

        println("creating task")
        val id = Random.nextInt(0, 10000)
        taskRepository.save(Task(id, taskRequest.name, taskRequest.startOn))

        val jobADetails = JobBuilder.newJob(SampleJob::class.java).withIdentity(taskRequest.name)
                .setJobData(JobDataMap(mapOf("id" to id)))
                .storeDurably().build()
        val trigger = TriggerBuilder.newTrigger().forJob(jobADetails)
                .withIdentity(taskRequest.name + "trigger")
                .startAt(Date.from(taskRequest.startOn))
                .build()

        scheduler.scheduleJob(jobADetails, trigger)
    }
}

data class TaskRequest(val name: String, val startOn: Instant)

@Component
class SampleJob : Job {

    @Throws(JobExecutionException::class)
    override fun execute(context: JobExecutionContext) {
        println("executing sample job ... ID: ${context.jobDetail.jobDataMap["id"]} at ${Instant.now()}")
    }
}
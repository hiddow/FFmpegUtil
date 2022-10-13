import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.Executors

object ShellUtils {
    private val threadpool = Executors.newCachedThreadPool()
    private fun run(waitFor: Boolean, command: Array<String>, listener: OnCommandExecOutputListener): Boolean {
        var ret = false
        try {
            val fullCmd = StringBuilder()
            for (s in command) {
                fullCmd.append(s)
                fullCmd.append(" ")
            }

            println("exec cmd: ${fullCmd.deleteAt(fullCmd.length - 1)}")

            val pid = Runtime.getRuntime().exec(command)
            threadpool.submit(OutputHandler(pid.inputStream, OutputHandler.TYPE_RETRIVE_OUTPUTSTREAM, listener))
            threadpool.submit(OutputHandler(pid.errorStream, OutputHandler.TYPE_RETRIVE_ERRORSTREAM, listener))
            ret = (if (waitFor) pid.waitFor() else pid.exitValue()) == 0
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: IllegalThreadStateException) {
            e.printStackTrace()
        }
        return ret
    }

    fun runAsync(command: Array<String>, listener: OnCommandExecOutputListener): Boolean {
        return run(false, command, listener)
    }

    fun run(command: Array<String>, listener: OnCommandExecOutputListener): Boolean {
        return run(true, command, listener)
    }

    fun run(command: String, listener: OnCommandExecOutputListener): Boolean {
        val commands =  command.split(" ").toTypedArray()
        return run(true, commands, listener)
    }

    interface OnCommandExecOutputListener {
        fun onSuccess(line: String?)
        fun onError(line: String?)
    }

    private class OutputHandler(
        private val `in`: InputStream,
        private val type: Int,
        private val listener: OnCommandExecOutputListener?
    ) : Runnable {
        override fun run() {
            try {
                BufferedReader(InputStreamReader(`in`)).use { bufr ->
                    var line: String? = null
                    while (bufr.readLine().also { line = it } != null) {
                        if (listener != null) {
                            if (type == TYPE_RETRIVE_ERRORSTREAM) {
                                listener.onError(line)
                            } else {
                                listener.onSuccess(line)
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        companion object {
            const val TYPE_RETRIVE_OUTPUTSTREAM = 0
            const val TYPE_RETRIVE_ERRORSTREAM = 1
        }
    }
}
package de.wegmann.wrapper.mp4v2

import groovy.util.logging.Log4j

/**
 * User: andy
 * Date: 01.06.13
 */
@Log4j
class Mp4Chaps extends AbstractMp4Tool {
    /**
     * Constructor.
     * @param mp4File mp4 file to operate on.
     */
    Mp4Chaps(String mp4File) {
        super(mp4File)
    }

    /**
     * Export chapters of mp4 file to a String. Contains the content of the file generated by
     * export command to mp4chaps.
     * @return chapter description
     */
    String exportChapters() {
        def process = [tools.mp4ChapsExecutable, "--export", mp4File].execute()
        process.waitFor()

        if (process.exitValue() != 0) {
            def errorText = process.err.text
            println errorText
            throw new WrapperException(msg: "error exporting chapters", execOutput: errorText)
        }

        def output = process.text
        if (output.startsWith("Exporting")) {
            def chapterFileMatch = output =~ /chapter file "(.*)"/
            def chapterFileName = chapterFileMatch[0][1]
            log.info "Chapterfile is $chapterFileName"
            File chapterFile = new File(chapterFileName)
            String chapterDefinitions = chapterFile.text
            chapterFile.delete()
            return chapterDefinitions
        } else {
            throw new WrapperException(msg: "found no chapter file name in output", execOutput: output)
        }
    }

    /**
     * import chapters of mp4 file to a String. Contains the content of the file generated by
     * export command to mp4chaps.
     * @return chapter description
     */
    void importChapters(String chapterDescription) {
        def lastIndexOfDot = this.mp4File.lastIndexOf('.')
        if (lastIndexOfDot == -1) {
            throw new WrapperException(msg: "mp4File seems not to contain an extension")
        }

        String chaptersFilename = this.mp4File.substring(0, lastIndexOfDot) + ".chapters.txt"
        File chaptersFile = new File(chaptersFilename)
        chaptersFile.text = chapterDescription

        try {
            def process = [tools.mp4ChapsExecutable, "--import", mp4File].execute()
            process.waitFor()

            if (process.exitValue() != 0) {
                def errorText = process.err.text
                println errorText
                throw new WrapperException(msg: "error importing chapters", execOutput: errorText)
            }

            def output = process.text
            if (output.startsWith("Importing")) {
                log.info("imported chapters")
            }
        } finally {
            chaptersFile.delete()
        }

    }
}

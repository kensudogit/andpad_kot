package jp.andpad.api.graphql.input

object LearningInputs {

    data class UpdateWatchProgressInput(
        val videoId: String,
        val learnerId: String,
        val positionSec: Int,
        val completed: Boolean?,
    )

    data class CreateVideoNoteInput(
        val videoId: String,
        val learnerId: String,
        val timestampSec: Int,
        val body: String,
    )

    data class SubmitQuizAttemptInput(
        val quizId: String,
        val learnerId: String,
        val answers: List<Int>,
    )

    data class CreateRagDocumentInput(
        val title: String,
        val content: String,
        val tags: List<String>,
    )

    data class CreateApiIntegrationInput(
        val name: String,
        val provider: String,
        val endpointUrl: String,
        val apiKeyHint: String,
    )

    data class CreateBimModelInput(
        val projectId: String,
        val title: String,
        val format: String,
        val viewerUrl: String,
        val fileSizeMb: Double?,
        val uploadedBy: String,
    )
}

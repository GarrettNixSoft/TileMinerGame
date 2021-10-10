package com.floober.engine.renderEngine.shaders;

import com.floober.engine.util.Logger;
import com.floober.engine.util.file.FileUtil;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryUtil.*;

public abstract class ShaderProgram {
	
	private final int programID;
	private final Integer[] vertexShaderIDs;
	private final Integer[] fragmentShaderIDs;

	private static FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

	public ShaderProgram(String vertexFile, String fragmentFile) {
		vertexShaderIDs = new Integer[1];
		vertexShaderIDs[0] = loadShader(vertexFile, GL_VERTEX_SHADER);
		fragmentShaderIDs = new Integer[1];
		fragmentShaderIDs[0] = loadShader(fragmentFile, GL_FRAGMENT_SHADER);
		programID = glCreateProgram();
		glAttachShader(programID, vertexShaderIDs[0]);
		glAttachShader(programID, fragmentShaderIDs[0]);
		bindAttributes();

		glLinkProgram(programID);
		int result = glGetProgrami(programID, GL_LINK_STATUS);
		if (result == GL_FALSE) {
			Logger.logError("Shader program linking failed.");
			Logger.log("glGetError(): " + glGetError());
			Logger.log(GL20.glGetProgramInfoLog(programID));
			System.exit(-1);
		}
		glValidateProgram(programID);
		getAllUniformLocations();

	}

	public ShaderProgram(ShaderCode... shaders) {
		programID = glCreateProgram();
		List<Integer> vertexShaders = new ArrayList<>();
		List<Integer> fragmentShaders = new ArrayList<>();
		for (ShaderCode shaderCode : shaders) {
			List<Integer> targetList = shaderCode.shaderType() == GL_VERTEX_SHADER ? vertexShaders : fragmentShaders;
			int shaderID = loadShader(shaderCode.shaderFile(), shaderCode.shaderType());
			glAttachShader(programID, shaderID);
			targetList.add(shaderID);
		}
		vertexShaderIDs = vertexShaders.toArray(new Integer[]{});
		fragmentShaderIDs = fragmentShaders.toArray(new Integer[]{});
		bindAttributes();
		glLinkProgram(programID);
		glValidateProgram(programID);
		getAllUniformLocations();
		
		int result = glGetProgrami(programID, GL_LINK_STATUS);
		if (result == GL_FALSE) {
			Logger.logError("Shader program linking failed.");
			System.exit(-1);
		}
	}

	protected abstract void getAllUniformLocations();

	protected int getUniformLocation(String uniformName) {
		return glGetUniformLocation(programID, uniformName);
	}
	
	public void start() {
		glUseProgram(programID);
	}
	
	public void stop() {
		glUseProgram(0);
	}
	
	public void cleanUp() {
		stop();
		for (int vertexShaderID : vertexShaderIDs) {
			glDetachShader(programID, vertexShaderID);
			glDeleteShader(vertexShaderID);
		}
		for (int fragmentShaderID : fragmentShaderIDs) {
			glDetachShader(programID, fragmentShaderID);
			glDeleteShader(fragmentShaderID);
		}
		glDeleteProgram(programID);
	}
	
	protected abstract void bindAttributes();
	
	protected void bindAttribute(int attribute, String variableName) {
		glBindAttribLocation(programID, attribute, variableName);
	}

	protected void loadFloat(int location, float value) {
		glUniform1f(location, value);
	}

	protected void loadInt(int location, int value) { glUniform1i(location, value); }

	protected void loadVector(int location, Vector4f vector) { glUniform4f(location, vector.x, vector.y, vector.z, vector.w); }

	protected void loadVector(int location, Vector3f vector) {
		glUniform3f(location, vector.x, vector.y, vector.z);
	}

	protected void loadVector(int location, Vector2f vector) {
		glUniform2f(location, vector.x, vector.y);
	}

	protected void loadFloat2(int location, float f1, float f2) {
		glUniform2f(location, f1, f2);
	}

	protected void loadBoolean(int location, boolean value) {
		glUniform1i(location, value ? 1 : 0);
	}

	protected void loadMatrix(int location, Matrix4f matrix) {
		matrixBuffer = matrix.get(matrixBuffer);
		glUniformMatrix4fv(location, false, matrixBuffer);
	}

	private static int loadShader(String file, int type) {
		List<String> lines = FileUtil.getFileData(file);
		StringBuilder shaderSource = new StringBuilder();
		for (String line : lines) {
			shaderSource.append(line).append("//\n");
		}
		int shaderID = GL20.glCreateShader(type);
		GL20.glShaderSource(shaderID, shaderSource);
		GL20.glCompileShader(shaderID);
		if(GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE){
			System.out.println(GL20.glGetShaderInfoLog(shaderID, 500));
			System.err.println("Could not compile shader!");
			System.exit(-1);
		}
		return shaderID;
	}
	
}
package com.ezardlabs.dethsquare.util;

import java.util.Arrays;

final class Matrix {

	static void clear(float[] matrix) {
		Arrays.fill(matrix, 0);
	}

	static void setIdentityM(float[] matrix) {
		for (int i = 0; i < 16; i++) {
			matrix[i] = 0;
		}
		for (int i = 0; i < 16; i += 5) {
			matrix[i] = 1.0f;
		}
	}

	static float length(float x, float y, float z) {
		return (float) Math.sqrt(x * x + y * y + z * z);
	}

	static void multiplyMM(float[] result, float[] lhs, float[] rhs) {
		for (int a = 0; a <= 12; a += 4) {
			for (int b = 0; b < 4; b++) {
				int c;
				int d;
				for (c = 0, d = 0; c < 4; c++, d += 4) {
					result[a + b] += rhs[a + c % 4] * lhs[d + b % 4];
				}
			}
		}
	}

	static void multiplyMV(float[] result, float[] lhsMatrix, float[] rhsVector) {
		result[0] = lhsMatrix[0] * rhsVector[0] + lhsMatrix[4] * rhsVector[1] +
				lhsMatrix[4 * 2] * rhsVector[2] + lhsMatrix[4 * 3] * rhsVector[3];
		result[1] = lhsMatrix[1] * rhsVector[0] + lhsMatrix[1 + 4] * rhsVector[1] +
				lhsMatrix[1 + 4 * 2] * rhsVector[2] + lhsMatrix[1 + 4 * 3] * rhsVector[3];
		result[2] = lhsMatrix[2] * rhsVector[0] + lhsMatrix[2 + 4] * rhsVector[1] +
				lhsMatrix[2 + 4 * 2] * rhsVector[2] + lhsMatrix[2 + 4 * 3] * rhsVector[3];
		result[3] = lhsMatrix[3] * rhsVector[0] + lhsMatrix[3 + 4] * rhsVector[1] +
				lhsMatrix[3 + 4 * 2] * rhsVector[2] + lhsMatrix[3 + 4 * 3] * rhsVector[3];
	}

	static void orthoM(float[] result, float left, float right, float bottom, float top, float near,
			float far) {
		if (left == right) {
			throw new IllegalArgumentException("left == right");
		}
		if (bottom == top) {
			throw new IllegalArgumentException("bottom == top");
		}
		if (near == far) {
			throw new IllegalArgumentException("near == far");
		}
		final float w = 1.0f / (right - left);
		final float h = 1.0f / (top - bottom);
		final float d = 1.0f / (far - near);
		final float x = 2.0f * w;
		final float y = 2.0f * h;
		final float z = -2.0f * d;
		final float tx = -(right + left) * w;
		final float ty = -(top + bottom) * h;
		final float tz = -(far + near) * d;
		result[0] = x;
		result[5] = y;
		result[10] = z;
		result[12] = tx;
		result[13] = ty;
		result[14] = tz;
		result[15] = 1.0f;
		result[1] = 0.0f;
		result[2] = 0.0f;
		result[3] = 0.0f;
		result[4] = 0.0f;
		result[6] = 0.0f;
		result[7] = 0.0f;
		result[8] = 0.0f;
		result[9] = 0.0f;
		result[11] = 0.0f;
	}

	static void setLookAtM(float[] result, float eyeX, float eyeY, float eyeZ, float centerX,
			float centerY, float centerZ, float upX, float upY, float upZ) {
		float fx = centerX - eyeX;
		float fy = centerY - eyeY;
		float fz = centerZ - eyeZ;
		float rlf = 1.0f / Matrix.length(fx, fy, fz);
		fx *= rlf;
		fy *= rlf;
		fz *= rlf;
		float sx = fy * upZ - fz * upY;
		float sy = fz * upX - fx * upZ;
		float sz = fx * upY - fy * upX;
		float rls = 1.0f / Matrix.length(sx, sy, sz);
		sx *= rls;
		sy *= rls;
		sz *= rls;
		float ux = sy * fz - sz * fy;
		float uy = sz * fx - sx * fz;
		float uz = sx * fy - sy * fx;
		result[0] = sx;
		result[1] = ux;
		result[2] = -fx;
		result[3] = 0.0f;
		result[4] = sy;
		result[5] = uy;
		result[6] = -fy;
		result[7] = 0.0f;
		result[8] = sz;
		result[9] = uz;
		result[10] = -fz;
		result[11] = 0.0f;
		result[12] = 0.0f;
		result[13] = 0.0f;
		result[14] = 0.0f;
		result[15] = 1.0f;
		translateM(result, -eyeX, -eyeY, -eyeZ);
	}

	static void translateM(float[] m, float x, float y, float z) {
		for (int i = 0; i < 4; i++) {
			m[12 + i] += m[i] * x + m[4 + i] * y + m[8 + i] * z;
		}
	}
}

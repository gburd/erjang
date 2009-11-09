package org.erlang;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.erlang.jbeam.ops.CodeAdapter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class EString extends ETerm implements CharSequence {

	private static final Charset ISO_LATIN_1 = Charset.forName("ISO-8859-1");
	
	byte[] data;
	int hash;
	
	public EString(String value) {
		this.hash = value.hashCode();
		this.data = value.getBytes(ISO_LATIN_1);
	}

	@Override
	public int hashCode() {
		return hash;
	}
	
	public String stringValue() {
		return new String(data, ISO_LATIN_1);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EString) {
			EString es = (EString) obj;
			return es.hash == hash && Arrays.equals(es.data, data);

		} else if (obj instanceof String) {
			if ( hash == obj.hashCode() ) {
				
			}
			
		}
		
		return false;
	}

	@Override
	public char charAt(int index) {
		return (char) (data[index] & 0xff);
	}

	@Override
	public int length() {
		return data.length;
	}

	@Override
	public CharSequence subSequence(final int start, final int end) {		
		return new SubSequence(start, end-start);
	}
	
	
	public class SubSequence implements CharSequence {

		private final int offset;
		private final int length;

		public SubSequence(int start, int length) {
			this.offset = start;
			this.length = length;
			EString.this.check_subseq(offset, length);
		}

		@Override
		public char charAt(int index) {
			return EString.this.charAt(offset+index);
		}

		@Override
		public int length() {
			return length;
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return new SubSequence(this.offset+start, end-start);
		}

	}

	void check_subseq(int offset, int length) {
		if (offset < 0 || length < 0 || (offset+length) > data.length)
			throw new IllegalArgumentException();
	}
	
	@Override
	public String toString() {
		return '"' + stringValue() + '"';
	}

	public static EString fromString(String s) {
		return new EString(s);
	}

	private static final Type ESTRING_TYPE = Type.getType(EString.class);
	private static final Type STRING_TYPE = Type.getType(String.class);

	@Override
	public Type emit_const(CodeAdapter fa) {

		Type type = ESTRING_TYPE;
		
		fa.visitLdcInsn(this.stringValue());
		fa.visitMethodInsn(Opcodes.INVOKESTATIC, type.getInternalName(),
					"fromString", "(" + STRING_TYPE.getDescriptor() + ")" + type.getDescriptor());
		
		return type;
	}


}

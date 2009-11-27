/**
 * This file is part of Erjang - A JVM-based Erlang VM
 *
 * Copyright (c) 2009 by Trifork
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package erjang;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ERT {
	
	public static EObject raise(EObject kind, EObject value, EObject trace) {

		// TODO: fix exception
		throw new ErlangException(kind.testAtom());
	}

	public static final EAtom AM_BADARG = EAtom.intern("badarg");
	public static final EAtom AM_BADMATCH = EAtom.intern("badmatch");
	public static final EAtom AM_BADARITH = EAtom.intern("badarith");
	public static final EAtom AM_MODULE = EAtom.intern("module");

	public static ECons cons(EObject h, EObject t) {
		return t.cons(h);
	}

	public static ErlangException badarith(Object... args) {
		throw new ErlangException(AM_BADARITH, args);
	}

	public static ErlangException badarg() {
		throw new ErlangException(AM_BADARG);
	}

	public static ErlangException badarg(Throwable cause, Object... args) {
		throw new ErlangException(AM_BADARG, cause, args);
	}

	public static ErlangException badarg(Object... args) {
		throw new ErlangException(AM_BADARG, args);
	}

	public static ErlangException badarg(Object o1, Object o2) {
		throw new ErlangException(AM_BADARG, new Object[] { o1, o2 });
	}

	public static final EAtom TRUE = EAtom.intern("true");
	public static final EAtom FALSE = EAtom.intern("false");

	public static boolean eq(EObject o1, EObject o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	public static boolean eq(EAtom o1, EAtom o2) {
		return o1 == o2;
	}

	public static EAtom as_atom_or_null(EObject o) {
		return o == null ? null : o.testAtom();
	}

	public static ECons as_nonempty_list_or_null(EObject o) {
		return o == null ? null : o.testNonEmptyList();
	}

	public static ENil as_nil_or_null(EObject o) {
		return o == null ? ERT.NIL : o.testNil();
	}

	public static EDouble as_float_or_null(EObject o) {
		return o == null ? null : o.testFloat();
	}

	/**
	 * @param s
	 * @return
	 */
	public static EPID loopkup_pid(EString name) {
		throw new NotImplemented();
	}

	// "definer" holds a reference to ClassLoader#defineClass
	static private final Method definer;
	static {
		try {
			definer = ClassLoader.class.getDeclaredMethod("defineClass",
					new Class[] { String.class, byte[].class, int.class,
							int.class });
			definer.setAccessible(true);
		} catch (Exception e) {
			throw new ErlangError(e);
		}
	}

	/**
	 * @param classLoader
	 * @param name
	 * @param data
	 * @param i
	 * @param length
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<? extends T> defineClass(ClassLoader classLoader,
			String name, byte[] data, int i, int length) {

		/*
		 * Class<? extends ETuple> res = (Class<? extends ETuple>)
		 * loader2.define(name, data);
		 */
		Class<? extends T> res;
		try {
			res = (Class<? extends T>) definer.invoke(ETuple.class
					.getClassLoader(), name.replace('/', '.'), data, 0, data.length);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		if (!name.equals(res.getName())) {
			throw new Error();
		}

		return res;
	}

	/**
	 * @param mod
	 * @param bin
	 */
	public static ETuple2 load_module(EAtom mod, EBinary bin) {

		EModule.load_module(mod, bin);

		return (ETuple2) ETuple.make(AM_MODULE, mod);
	}

	
	public static ESmall box(int i) {
		return new ESmall(i);
	}
	
	/**
	 * Boxes a <code>long</code> value to an EInteger (EBig or ESmall)
	 * 
	 * @param longValue
	 * @return
	 */
	public static EInteger box(long longVal) {
		
		// very simple: see if the longValue can be converted
		// to an int and back again without loosing it's value
		
		int intVal = (int) longVal;
		if (longVal == (long)intVal) {
			return new ESmall(intVal);
		} else {
			return new EBig(longVal);			
		}
	}
	
	public static EInteger box2(long longVal) {
			

		// compute l's offset from Integer.MIN_VALUE
		long offset_from_int_min = longVal - (long) Integer.MIN_VALUE;
		
		// strip sign bit
		long unsigned_offset = offset_from_int_min & Long.MAX_VALUE;

		if (unsigned_offset >= 0x100000000L) {
			return new EBig(longVal);
		} else {
			return new ESmall((int) longVal);
		}

	}

	/**
	 * @param doubleVal
	 * @return
	 */
	public static EDouble box(double doubleVal) {
		return new EDouble(doubleVal);
	}

	static BigInteger INT_MIN_AS_BIG = BigInteger.valueOf(Integer.MIN_VALUE);
	static BigInteger INT_MAX_AS_BIG = BigInteger.valueOf(Integer.MAX_VALUE);
	private static ENode localNode;

	/**
	 * @param add
	 * @return
	 */
	public static EInteger box(BigInteger res) {
		
		if (res.compareTo(INT_MIN_AS_BIG) < 0) 
			return new EBig(res);
		
		if (res.compareTo(INT_MAX_AS_BIG) > 0)
			return new EBig(res);

		return new ESmall(res.intValue());
	}

	/**
	 * @param b
	 * @return
	 */
	public static EAtom box(boolean bool) {
		return bool ? TRUE : FALSE;
	}

	/**
	 * @param b
	 * @return
	 */
	public static EAtom guard(boolean bool) {
		return bool ? TRUE : null;
	}

	public static final ENil NIL = new ENil();
	public static final EAtom EXIT = EAtom.intern("EXIT");
	public static final EAtom IGNORED = EAtom.intern("ignored");
	private static final EAtom am_badmatch = EAtom.intern("badmatch");

	public EBitStringBuilder bs_init(int size, int flags) {
		return new EBitStringBuilder(size, flags);
	}

	/**
	 * @param e
	 * @return
	 */
	public static ESmall get_posix_code(IOException e) {
		
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param owner
	 * @param make
	 */
	public static void send(EObject pid, EObject msg) {
		EPID p;
		if ((p=pid.testPID()) == null) {
			throw badarg(pid, msg);
		}
		
		p.send(msg);
	}

	/**
	 * @return
	 */
	public static ENode getLocalNode() {
		return localNode;
	}

	static EAtom am_undef = EAtom.intern("undef");
	
	/**
	 * @param fun
	 * @return
	 */
	public static ErlangException undef(FUN fun, EObject... args) {
		throw new ErlangException(am_undef, args);
	}

	public static EObject apply$last(EProc proc, EObject arg1, EObject mod, EObject fun)
	{
		EAtom m = mod.testAtom();
		EAtom f = fun.testAtom();
		
		if (m==null||f==null) throw ERT.badarg(mod, fun, arg1);
		
		return EModule.resolve(new FUN(m,f,1)).invoke(proc, NIL.cons(arg1).toArray());
	}

	static Map<EAtom,EObject> register = new ConcurrentHashMap<EAtom, EObject>();
	
	/**
	 * @param aname
	 * @param eObject
	 */
	public static void register(EAtom aname, EObject eObject) {
		register.put(aname, eObject);
	}
	
	public static EObject badmatch(EObject val) {
		throw new ErlangException(am_badmatch, val);
	}
	
}

package datasphere.shims.facebook;

import java.util.*;

/**
 * Adapted from a Java port written by Jesper Juul, which was based 
 * on a BASIC port of Eliza that he found floating on the net. 
 * Eliza was originally written in 1966 by Joseph Weizenbaum. 
 * This version is not far at all from the original.
 **/

public class HorizonBot
{
	private static String lastline = "";
	private static int noQuestionsAnswered = 0;
	
	HashMap< String, String > wordinout = new HashMap< String, String >();
	{
		{
			wordinout.put(" are "," am ");
			wordinout.put(" were "," was ");
			wordinout.put(" you "," I ");
			wordinout.put(" your "," my ");
			wordinout.put(" i've "," you've ");
			wordinout.put(" i'm "," you're ");
			wordinout.put(" me "," you ");		
		}
	};

	private static class Keyword {
		public String word;
		public int replyStartIndex;
		public int replyCurrentIndex;
		public int noReplies;
		
		public Keyword(String word, int replyIndex, int noReplies) {
			
			this.word = word;
			this.replyStartIndex = replyIndex;
			this.replyCurrentIndex = replyIndex;
			this.noReplies = noReplies;
		}
		
		public void increment() {
			
			replyCurrentIndex++;
			if ( replyCurrentIndex - replyStartIndex >= noReplies ) 
				replyCurrentIndex = replyStartIndex;
		}
		
		@Override
		public String toString() {
			return word + "(" + replyStartIndex + ", " + noReplies + "," + replyCurrentIndex + ")";
		}
	}
	
	private static Vector< Keyword > keywords = new Vector< Keyword >();
	{
		{
			keywords.add( new Keyword( "can you ",  0, 3 ) ); 
			keywords.add( new Keyword( "can i ", 3, 2 ) ); 
			keywords.add( new Keyword( "you are ",5, 4 ) ); 
			keywords.add( new Keyword( "you're ", 5, 4 ) ); 
			keywords.add( new Keyword( "i don't ", 9, 4 ) ); 
			keywords.add( new Keyword( "i feel ", 13, 3 ) ); 
			keywords.add( new Keyword( "why don't you ", 16, 3 ) ); 
			keywords.add( new Keyword( "why can't i ", 19, 2 ) ); 
			keywords.add( new Keyword( "are you ", 21, 3 ) ); 
			keywords.add( new Keyword( "i can't ", 24, 3 ) ); 
			keywords.add( new Keyword( "i am ", 27, 4 ) ); 
			keywords.add( new Keyword( "i'm ", 27, 4 ) ); 
			keywords.add( new Keyword( "you ", 31, 3 ) ); 
			keywords.add( new Keyword( "i want ", 34, 5 ) ); 
			keywords.add( new Keyword( "what ", 39, 9 ) ); 
			keywords.add( new Keyword( "how ", 39, 9 ) ); 
			keywords.add( new Keyword( "who ", 39, 9 ) ); 
			keywords.add( new Keyword( "where ", 39, 9 ) ); 
			keywords.add( new Keyword( "when ", 39, 9 ) ); 
			keywords.add( new Keyword( "why ", 39, 9 ) ); 
			keywords.add( new Keyword( "name ", 48, 2 ) ); 
			keywords.add( new Keyword( "cause ", 50, 4 ) ); 
			keywords.add( new Keyword( "sorry ", 54, 4 ) ); 
			keywords.add( new Keyword( "dream ", 58, 4 ) ); 
			keywords.add( new Keyword( "hello ", 62, 1 ) ); 
			keywords.add( new Keyword( "hi ", 62, 1 ) ); 
			keywords.add( new Keyword( "maybe ", 63, 5 ) ); 
			keywords.add( new Keyword( "no", 68, 5 ) ); 
			keywords.add( new Keyword( "your ", 73, 2 ) ); 
			keywords.add( new Keyword( "always ", 75, 4 ) ); 
			keywords.add( new Keyword( "think ", 79, 3 ) ); 
			keywords.add( new Keyword( "alike ", 82, 7 ) ); 
			keywords.add( new Keyword( "yes ", 89, 3 ) ); 
			keywords.add( new Keyword( "friend ", 92, 6 ) ); 
			keywords.add( new Keyword( "computer", 98, 7 ) ); 
			keywords.add( new Keyword( "nokeyfound", 105, 6 ) );
		} 
	};

	/*
	 * 
	 */
	public String handleLine( String input ){
		
		noQuestionsAnswered++;
		String msg = "";
		Keyword match = keywords.get( keywords.size() - 1 );;
		
		input = "  " + input.toLowerCase() + "  ";
		input = removeChar(input, '\'');
	
		//-- does the input tell me to shut down?
		if ( input.indexOf( "shut" ) >= 0 ) {
			return "O.K. if you feel that way I will shut up....";
		}
	
		//-- has the same item been sent twice?
		if ( input.equals( lastline ) ) {
			return "You appear to be repeating yourself.";
		}
	
		lastline = input;
		
		int pos = 0;
		int keyIndex;
		String C = "error";
		
		//-- trys to find one of the keywords...
		for ( keyIndex = 0; keyIndex < keywords.size(); keyIndex++ ) {
			pos = input.indexOf( keywords.get( keyIndex ).word );
			
			//-- if we find one..
			if ( pos >= 0 ) {
				
				//-- if the match is "I WANT"...
				if ( keyIndex == 13 )
					//-- then "ALWAYS" can override. Don't know why...
					if ( input.indexOf( keywords.get( keyIndex ).word ) >= 0 ) 
						keyIndex = 29; 
	
				match = keywords.get( keyIndex );
				
				//-- examine the rest of the sentence after the keyword
				C = input.substring( pos + match.word.length() - 1 );

				//-- replace any of the wordins with the wordouts.
				for ( Map.Entry< String, String > entry : wordinout.entrySet()) 
					C = C.replaceAll( entry.getKey(), entry.getValue() );
				
				//-- Remove extra spaces
				C = C.replaceAll("  ", " ");
				
				break;
			}
		}
		
		match.increment();
			
		//-- generate a reply from the list
		String reply = REPLIES[ match.replyCurrentIndex ];
		
			
		if ( reply.charAt( reply.length() - 1 ) != '*') {
			msg += reply;
		}
		else {
			if (C.equals("   ")) {
				msg += ("You will have to elaborate more than that if I am to help you.");
			}
			else {
				msg += reply.substring( 0, reply.length() - 1 ) + C ;
			}
		}

		return msg;
	}

	
	static String REPLIES[]=
	{
		"Don't you believe that a Horizon bot can*",
		"What makes you think that I can*",
		"You want me and Horizon to be able to*",
		"Perhaps you don't want to*",
		"Do you want to be able to*",
		"What makes you think I am*",
		"Noone else in Horizon believes that I am*",
		"Perhaps you would like to be*",
		"Do you sometimes wish you were*",
		"Don't you really*",
		"Why don't you*",
		"Do you wish to be able to*",
		"Does that trouble you*? The possibility of the Horizon Coffee machine breaking troubles me.",
		"Do you often feel*",
		"We at Horizon wonder if you often feel*",
		"Do you enjoy feeling*",
		"Do you really believe i don't*",
		"Perhaps in good time Horizon will allow me to*",
		"Do you want me to*",
		"Do you think you should be able to*",
		"Why can't you*",
		"Why are you interested in whether or not i am*",
		"Would you prefer if i were not*",
		"Perhaps in your fantasies i am*",
		"How do you know you can't*",
		"Have you tried? Perhaps we can get a Horizon intern to help?",
		"Perhaps you can now*",
		"Did you come to me because you are*",
		"How long have you been*",
		"Do you believe it is normal to be*",
		"Do you enjoy being*",
		"We were discussing you--not me.",
		"Oh, i*",
		"You're not really talking about me, are you?",
		"What would it mean to you if you got*",
		"Why do you want*",
		"Suppose you soon got*",
		"What if you never got*",
		"I sometimes also want*",
		"Why do you ask?",
		"Does that question interest you?",
		"What answer would please you the most?",
		"What do you think?",
		"Are such questions on your mind often?",
		"What is it that you really want to know?",
		"Have you asked anyone else?",
		"Have you asked such questions before?",
		"What else comes to mind when you ask that?",
		"Names don't interest me.",
		"I don't care about names --please go on.",
		"Is that the real reason?",
		"Don't any other reasons come to mind?",
		"Does that reason explain anything else?",
		"What other reasons might there be?",
		"Please don't apologize!",
		"Apologies are not necessary.",
		"What feelings do you have when you apologize?",
		"Don't be so defensive!",
		"What does that dream suggest to you?",
		"Do you dream often?",
		"What persons appear in your dreams?",
		"Are you disturbed by your dreams?",
		"Hi, I am the first Horizon digital agent... I'm very stupid at the moment, but I am a start. Please state your problem.",
		"You don't seem quite certain.",
		"Why the uncertain tone?",
		"Can't you be more positive?",
		"You aren't sure?",
		"Don't you know?",
		"Are you saying no just to be negative?",
		"You are being a bit negative.",
		"Why not?",
		"Are you sure?",
		"Why no?",
		"Why are you concerned about my*",
		"What about your own*",
		"Can you think of a specific example?",
		"When?",
		"What are you thinking of?",
		"Really, always?",
		"Do you really think so?",
		"But you are not sure you*",
		"Do you doubt you*",
		"In what way?",
		"What resemblance do you see?",
		"What does the similarity suggest to you?",
		"What other connections do you see?",
		"Could there really be some connection?",
		"How?",
		"You seem quite positive.",
		"Are you sure?",
		"I see.",
		"I understand.",
		"Why do you bring up the topic of friends?",
		"Do your friends worry you?",
		"Do your friends pick on you?",
		"Are you sure you have any friends?",
		"Do you impose on your friends?",
		"Perhaps your love for your friends worries you.",
		"Do computers worry you?",
		"Are you frightened by machines? I am more frightened by Sophie to be honest...",
		"Are you talking about me in particular?",
		"Why do you mention computers - I live in the cloud?",
		"What do you think machines have to do with your problem?",
		"Don't you think computers can help people. At Horizon we do?",
		"What is it about machines that worries you?",
		"Say, do you have any psychological problems. We have a mental health department just upstairs?",
		"What does that suggest to you?",
		"Got it.",
		"I'm not sure I understand you fully.",
		"Come come, elucidate your thoughts, and we'll see how Horizon can help ;)",
		"Can you elaborate on that?",
		"That is quite interesting. I will let mac know about it.",
	};


	/**
	Utility function that removes a char from a String.
	*/
	public static String removeChar( String s, char c )
	{ 
		if ( s == null ) return s;
		int p;
		while( ( p = s.indexOf( c ) ) >= 0 )
			s = s.substring( 0, p - 1 ) + s.substring( p + 1 );
		
		return s;
	}
}

/*
 * A -> <1, 2, 3>
 * 
 * 1) Pr(<1,2,3>, A), Pr(<1>, A), Pr(<1,2>, A), Pr(<2,3>,A), Pr(<2>,A), Pr(<3>,A)
 * 2) as above with Pr(<1,3>) also.
 * 3)
 * 
 * 
 */



#include <iostream>
#include <iomanip>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
#include <map>
#include <queue>
#include <time.h>

using namespace std;

struct Video {
	string title; //'title'
	unsigned long long views; //'statistics'
	unsigned int like;
	unsigned int dislike;
	string description; //'description'
};

void readFile(string fileName, vector<Video*>& videos, int& i) {
	ifstream youtube;
	youtube.open(fileName);
	string info;
	bool runningTitle = false;
	bool runningDescription = false;
	bool localizationVer = false;

	if (youtube.is_open()) {
			while (getline(youtube, info, ',')) {
				string s = info.substr(1, 11);
				string x = info.substr(1, 14);
				if (info.substr(1, 7) == "\'title\'") {
					Video* temp = new Video;
					temp->title = info.substr(11, info.size() - 12);
					videos.push_back(temp);
					runningTitle = true;
					continue;
				}
				else if (info.substr(1, 11) == "\'localized\'") {
					//so description isn't copied twice
					localizationVer = true;
				}
				else if (info.substr(1, 13) == "\'description\'" && !localizationVer) {
					//title has finished
					runningTitle = false;
					//updates based on if description has comma
					if (info.at(info.size() - 1) == '\'') {
						videos.at(i)->description += (info.substr(17, info.size() - 18));
					}
					else {
						videos.at(i)->description += (info.substr(17, info.size() - 17));
					}
					runningDescription = true;
					continue;
				}
				else if (info.substr(1, 12) == "\'thumbnails\'") {
					//description has finished
					runningDescription = false;
				}
				else if (info.substr(1, 12) == "\'statistics\'") {
					//localized info has finished
					localizationVer = false;
					videos.at(i)->views = stoll(info.substr(30, info.size() - 31));
				}
				else if (info.substr(1, 11) == "\'likeCount\'") {
					string temp = info.substr(15, info.size() - 16);
					videos.at(i)->like = stoi(info.substr(15, info.size() - 16));
				}
				else if (info.substr(1, 14) == "\'dislikeCount\'") {
					string temp = info.substr(18, info.size() - 19);
					videos.at(i)->dislike = stoi(info.substr(18, info.size() - 19));
					//now we can start the next video object
					i++;
				}
				//in case title or description include commas
				if (runningTitle) {
					videos.at(i)->title += info.substr(1, info.size() - 2);
				}
				else if (runningDescription) {
					videos.at(i)->description += info.substr(0, info.size() - 1);
				}
			}
	}
	youtube.close();
}

map<string, string> pos() {
	int i = 0;
	ifstream moby;
	moby.open("mobypos.txt");
	map<string, string> pos;

	//gets parts of speech from database
	if (moby.is_open()) {
		string line;

		while (getline(moby, line)) {
			string word = line.substr(0, line.find('\\'));
			string part = line.substr(line.find('\\') + 1);
			pos[word] = part;
		}
	}
	moby.close();
	return pos;
	/*Noun 			N
	Plural			p
	Noun Phrase		h
	Verb (usu participle)	V
	Verb (transitive)	t
	Verb (intransitive)  	i
	Adjective		A
	Adverb			v
	Conjunction		C
	Preposition		P
	Interjection		!
	Pronoun			r
	Definite Article	D
	Indefinite Article	I
	Nominative		o*/
}

void rankMePls(map<string, long double>& ranking, vector<Video*> vids) {
	for (int i = 0; i < vids.size(); i++) {
		string word;
		stringstream title;
		stringstream description;
		title.str(vids.at(i)->title);
		description.str(vids.at(i)->description);

		long double priority = vids.at(i)->views * (vids.at(i)->like / ((double)vids.at(i)->dislike + (double)vids.at(i)->like));
		long double dpriority = priority / 15.0;
		
		while (getline(title, word, ' ')) {
			if (word.size() > 0 && !isalpha(word.at(word.size() - 1))) {
				word = word.substr(0, word.size() - 1);
			}
			ranking[word] += priority;
		}

		while (getline(description, word, ' ')) {
			if (word.size() > 0 && !isalpha(word.at(word.size() - 1))) {
				word = word.substr(0, word.size() - 1);
			}
			ranking[word] += dpriority;
		}
	}
}

void getPOS(map<string, long double> ranked, map<string, string> pos, priority_queue<pair<long double, string>>& n, priority_queue<pair<long double, string>>& v, priority_queue<pair<long double, string>>& adj, priority_queue<pair<long double, string>>& adv) {
	for (auto iter = ranked.begin(); iter != ranked.end(); iter++) {
		if (pos[iter->first] == "") {
			continue;
		}
		else if (pos[iter->first] == "N" || pos[iter->first] == "p") {
			n.push(pair<long double,string>(iter->second, iter->first));
		}
		else if (pos[iter->first] == "V"|| pos[iter->first] == "t" || pos[iter->first] == "i") {
			v.push(pair<long double, string>(iter->second, iter->first));
		}
		else if (pos[iter->first] == "A") {
			adj.push(pair<long double, string>(iter->second, iter->first));
		}
		else if (pos[iter->first] == "v") {
			adv.push(pair<long double, string>(iter->second, iter->first));
		}
	}

}

void titleGenerate(int titleNum, vector<string>& n, vector<string>& v, vector<string>& adj, vector<string>& adv) {
	//use time to randomly generate
	srand(time(NULL));
	ofstream titles("titles.txt");

	for (int i = 0; i < titleNum; i++) {
		int titlePattern = rand() % 10 + 1;

		if (titlePattern == 1) {
			//adjective noun verb noun adverb
			titles << adj.at(rand() % adj.size()) << " ";
			titles << n.at(rand() % n.size()) << " ";
			titles << v.at(rand() % v.size()) << " ";
			titles << n.at(rand() % n.size()) << " ";
			titles << adv.at(rand() % adv.size()) << " ";
			titles << endl;
		}
		else if (titlePattern == 2) {
			//noun adverb verb adjective noun
			titles << n.at(rand() % n.size()) << " ";
			titles << adv.at(rand() % adv.size()) << " ";
			titles << v.at(rand() % v.size()) << " ";
			titles << adj.at(rand() % adj.size()) << " ";
			titles << adv.at(rand() % adv.size()) << " ";
			titles << endl;
		}
		else if (titlePattern == 3) {
			//adjective noun adverb verb
			titles << adj.at(rand() % adj.size()) << " ";
			titles << n.at(rand() % n.size()) << " ";
			titles << adv.at(rand() % adv.size()) << " ";
			titles << v.at(rand() % v.size()) << " ";
			titles << endl;
		}
		else if (titlePattern == 4) {
			//adjective noun, adjective noun, and adjective noun
			titles << adj.at(rand() % adj.size()) << " ";
			titles << n.at(rand() % n.size()) << ", ";
			titles << adj.at(rand() % adj.size()) << " ";
			titles << n.at(rand() % n.size()) << ", and ";
			titles << adj.at(rand() % adj.size()) << " ";
			titles << n.at(rand() % n.size()) << " ";
			titles << endl;
		}
		else if (titlePattern == 5) {
			//top ten adjective noun
			titles << "Top Ten " << adj.at(rand() % adj.size()) << " " << n.at(rand() % n.size()) << endl;
		}
		else if (titlePattern == 6) {
			//Social Experiment: verb noun adverb
			titles << "Social Experiment: " << v.at(rand() % v.size()) << " " << n.at(rand() % n.size()) << " " << (adv.at(rand() % adv.size())) << endl;
		}
		else if (titlePattern == 7) {
			titles << "I " << v.at(rand() % v.size()) << " " << n.at(rand() % n.size()) << " and this is what happened..." << endl;
		}
		else if (titlePattern == 8) {
			titles << "We " << (adv.at(rand() % adv.size())) << " " << v.at(rand() % v.size()) << " " << n.at(rand() % n.size()) << " in " << adj.at(rand() % adj.size()) << " " << n.at(rand() % n.size()) << endl;
		}
		else if (titlePattern == 9) {
			//adverb verb adjective noun
			titles << adv.at(rand() % adv.size()) << " ";
			titles << v.at(rand() % v.size()) << " ";
			titles << adj.at(rand() % adj.size()) << " ";
			titles << n.at(rand() % n.size()) << " ";
			titles << endl;
		}
		else {
			//adjective noun (Satisfying)
			titles << n.at(rand() % n.size()) << " ";
			titles << n.at(rand() % n.size()) << " ";
			titles << "(SATISFYING)" << endl;
		}
	}
	titles.close();
}

void top100(priority_queue<pair<long double, string>>& list, vector<string>& words) {
	for (int i = 0; i < 100; i++) {
		if (list.empty()) {
			break;
		}
		words.push_back(list.top().second);
		list.pop();
	}
}

int main(int argc, char* argv[]) {
	int num = stoi(argv[1]);
	vector<Video*> videos;
	int numVideos = 0;
	map<string, long double> ranked;
	map<string, string> partsOfSpeech;
	priority_queue<pair<long double, string>> nouns;
	priority_queue<pair<long double, string>> verbs;
	priority_queue<pair<long double, string>> adjectives;
	priority_queue<pair<long double, string>> adverbs;
	vector<string> n;
	vector<string> v;
	vector<string> adj;
	vector<string> adv;

	for (int i = 0; i < 20; i++) {
		string tempName = "..outputFiles/outputFile" + to_string(i) + ".txt";
		readFile(tempName, videos, numVideos);
	}
	partsOfSpeech = pos();
	rankMePls(ranked, videos);
	getPOS(ranked, partsOfSpeech, nouns, verbs, adjectives, adverbs);
	top100(nouns, n);
	top100(verbs, v);
	top100(adjectives, adj);
	top100(adverbs, adv);
	titleGenerate(num, n, v, adj, adv);

	return 0;
}

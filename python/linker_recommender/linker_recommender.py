from flask import Flask, request, Response, jsonify
#import pprint
import json
import spacy
# to load classifier and topic model
from joblib import load


app = Flask(__name__)

# https://spacy.io/usage/models
# python -m spacy download en_core_web_sm
_nlp = spacy.load("en_core_web_sm") # is faster
#_nlp = spacy.load("en_core_web_trf") # performs better

# Load recommender model
model = load('./models/label_features_f1.csv.model')
# Load LDA topic model
lda_model = load('./models/lda.model')


# Run with
# <s>flask run --port=5002</s>
# TODO Didn't work with spaCy, use
# python app.py

# Test with
# curl http://127.0.0.1:5002/ --header "Content-Type: application/json" --request POST -d '{"doc" : {"text": "Napoleon was the emperor of the First French Empire."}}'



# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
#                                           START PREDICTION CODE
# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 


ner_label_positions = ["CARDINAL","DATE","EVENT","FAC","GPE","LANGUAGE","LAW","LOC","MONEY","NORP","ORDINAL","ORG","PERCENT","PERSON","PRODUCT","QUANTITY","TIME","WORK_OF_ART"]

def document_characteristics(doc=""):
    vector_document_feature = []
    # document length
    if doc is None:
        doc = ""
    vector_document_feature.append(len(doc))
    # number of tokens - regex
    tokens_re = re.split('\\s+', doc)
    # if tokens_split is empty... add an entry
    if (tokens_re is None or len(tokens_re) == 0):
        tokens_re = [""]
    vector_document_feature.append(len(tokens_re))
    # max token length
    vector_document_feature.append(max([len(x) for x in tokens_re]))
    # min token length
    vector_document_feature.append(min([len(x) for x in tokens_re]))
    # avg
    vector_document_feature.append(mean([len(x) for x in tokens_re]))
    # number of tokens - basic split
    tokens_split = doc.split()
    vector_document_feature.append(len(tokens_split))
    
    
    # if tokens_split is empty... add an entry
    if (tokens_split is None or len(tokens_split) == 0):
        tokens_split = [""]
    # max token length
    vector_document_feature.append(max([len(x) for x in tokens_split]))
    # min token length
    vector_document_feature.append(min([len(x) for x in tokens_split]))
    # avg
    vector_document_feature.append(mean([len(x) for x in tokens_split]))
    # spacy
    spacy_doc = _nlp(doc)
    mentions = []
    # initialise distribution with zeros
    ner_label_distribution = [0 for ner_label in ner_label_positions]
    for ent in spacy_doc.ents:
        #print(f'[{ent.start_char}:{ent.end_char}] {ent.text} ({ent.label_})')
        #mention = spacy_ent_to_mention(ent)
        ner_label_index = ner_label_positions.index(ent.label_)
        # Increment distribution counter by one
        ner_label_distribution[ner_label_index] = ner_label_distribution[ner_label_index] + 1 
        #mentions.append(ent)
        #print(ent)
        #print(ent.text, ent.start_char, ent.end_char, ent.label_)
    #print(ner_label_distribution)
    #print(f'Detected {len(mentions)} mentions')

    # Add document's NER class distribution to the feature vector
    vector_document_feature.extend(ner_label_distribution)

    # Add sum of NER classes to doc features
    vector_document_feature.append(sum(ner_label_distribution))
    
    # Add avg (by distribution) of NER classes to doc features
    vector_document_feature.append(mean(ner_label_distribution))

    # Add avg (by token count) of NER classes to doc features
    vector_document_feature.append(sum(ner_label_distribution)/len(tokens_split))
    
    #print(vector_document_feature)
    
    return vector_document_feature
    
    
def sent_to_words(sentences):
    for sentence in sentences:
        # deacc=True removes punctuations
        yield(gensim.utils.simple_preprocess(str(sentence), deacc=True))
    
    
def remove_stopwords(texts):
    return [[word for word in simple_preprocess(str(doc)) 
             if word not in stop_words] for doc in texts]

    
def process_document_texts(docs=[""], display_some=True, lda_model=None):
    data_words = list(sent_to_words([doc.split(' ') if doc is not None else "" for doc in docs]))

    # print(data_words)
    # remove stop words
    data_words = remove_stopwords(data_words)

    # Term Document Frequency
    corpus = [id2word.doc2bow(doc) for doc in data_words]

    # View
    #print(corpus[:1][0][:30])
    if display_some:
        for index, doc in enumerate(corpus):
            #0 [(0, 0.050012715), (1, 0.050015952), (2, 0.050007522), (3, 0.54990005), (4, 0.050010785), (5, 0.05000899), (6, 0.05000739), (7, 0.050011847), (8, 0.050007924), (9, 0.05001683)]
            print(index, lda_model.get_document_topics(doc))
            # Only display first 100
            if index >= 100:
                break

    return corpus, data_words


def create_feature_vectors(lda_model, input_docs=[], display_some=True):
    corpus, data_words = process_document_texts(input_docs, display_some=display_some, lda_model=lda_model)

    all_features = []#np.empty(1)#np.array([])
    for index, doc in enumerate(corpus):
        vector_feature = []
        # the n topic dimensions for each document
        # print(index, lda_model.get_document_topics(doc))
        topic_vector = np.zeros(lda_model.num_topics)
        for topic in lda_model.get_document_topics(doc):
            topic_vector[topic[0]] = topic[1]
        # print(topic_vector)
        vector_feature.extend(topic_vector)

        # document characteristics...
        # document content: print(docs[index])
        vector_document_feature = document_characteristics(input_docs[index])
        vector_feature.extend(vector_document_feature)

        # Add this feature vector to the list of all feature vectors
        all_features.append(vector_feature)
    return all_features


def recommend_linker(model=None, doc="Hello world"):
    text_docs = [doc]
    X_features = create_feature_vectors(lda_model=lda_model, input_docs=text_docs, display_some=False)
    y_pred = model.predict(X_features)
    print("Predicting: ", y_pred)
    return y_pred


# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
#                                           END PREDICTION CODE
# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 


def process(doc):
    text = doc['text']
    recommended_linker = recommend_linker(model=model, doc=text)
    mention = {'offset' : 0, 'mention': recommended_linker}
    print("Created fake mention: ", mention)
    doc['mentions'] = [mention]
    print(f'Detected {len(doc["mentions"])} mentions')
    return doc


@app.route('/', methods=['get', 'post'])
def index():
    print(request.data)
    req = json.loads(request.data)
    document = req['document']
    pipelineConfig = req['pipelineConfig']
    componentId = req['componentId']

    # TODO The component has only one functionality, so no matter what is currentComponent, it there is only one way of processing the document
    #if findComponentInPipelineConfig(current_component).getType() is EnumComponentType.MD:

    document = process(document)

    return jsonify(
            {'document' : document,
            'pipelineConfig' : pipelineConfig,
            'componentId' : componentId}
            )


class LoggingMiddleware(object):
    def __init__(self, app):
        self._app = app

    def __call__(self, env, resp):
        errorlog = env['wsgi.errors']
        #pprint.pprint(('REQUEST', env), stream=errorlog)

        def log_response(status, headers, *args):
            #pprint.pprint(('RESPONSE', status, headers), stream=errorlog)
            return resp(status, headers, *args)

        return self._app(env, log_response)


# Run at flask startup (https://stackoverflow.com/a/55573732)
with app.app_context():
    pass

if __name__ == '__main__':
    app.wsgi_app = LoggingMiddleware(app.wsgi_app)
    #app.run(host='0.0.0.0', port=80)
    app.run(port=5002)
    #app.run()

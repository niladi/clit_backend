from flask import Flask, request, Response, jsonify
#import pprint
import json
import os
from tqdm import tqdm

app = Flask(__name__)

_candidate_dict = {}
#_path = 'wikidata_labels.dictionary.sample'
_path = 'wikidata_labels.dictionary'
_entity_replace_src = "http://wikidata.dbpedia.org/resource/"
_entity_replace_target = "http://www.wikidata.org/entity/"

# Run with
# flask run --port=5001

# Test with
# curl http://127.0.0.1:5000/ --header "Content-Type: application/json" --request POST -d '{"translator": "DB2WD"}'

def load_candidate_dict(path, replace_entity_src="", replace_entity_target="", delim_entity_label="\t\t",
        delim_label_to_label="\t"):
    print('Loading Wikidata candidate dictionary...')

    with tqdm(total=os.path.getsize(path)) as pbar:
        with open(path, 'r') as f:
            for line in f:
                pbar.update(len(line))

                # entity \t\t label1 \t label2 \t label3 \t...
                tokens = line.split(delim_entity_label)
                entity = tokens[0].strip()
                labels = tokens[1].strip().split(delim_label_to_label)

                # replace e.g. wikidata.dbpedia.org by wikidata.org
                if replace_entity_src:
                    entity = entity.replace(replace_entity_src, replace_entity_target)

                # add to dict
                for label in labels:
                    if label not in _candidate_dict.keys():
                        _candidate_dict[label] = [entity]
                    else: # label already existing in dict
                        _candidate_dict[label].append(entity)

    print(f'Loaded candidate dictionary with {len(_candidate_dict)} entries (= diff. labels).')


@app.route('/', methods=['get', 'post'])
def index():
    print(request.data)
    req = json.loads(request.data)
    label = req['label']
    res = _candidate_dict.get(label, [])
    #try:
    #    res = _candidate_dict[label]
    #except KeyError:
    #    print(f'"{label}" not in dictionary')
    #    res = []
    #return ", ".join(res)
    return jsonify(
            candidates = res
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
    load_candidate_dict(_path, _entity_replace_src, _entity_replace_target)

if __name__ == '__main__':
    app.wsgi_app = LoggingMiddleware(app.wsgi_app)
    app.run()
